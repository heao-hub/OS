package experiment03;

import java.util.*;

/**
 * 内存管理器
 */
public class MemoryManager {

    public int totalMemory;
    public List<Partition> partitionTable = new ArrayList<>();
    public Map<String,Process> processTable = new HashMap<>();
    public int partNum = 0;
    public AllocStrategy allocStrategy;

    public OutStrategy outStrategy;
    public List<Request> requestList = new ArrayList<>();
    public Integer requestCount = 0;


    public MemoryManager(int totalMemory,AllocStrategy allocStrategy,OutStrategy outStrategy){
        this.totalMemory = totalMemory;
        this.allocStrategy = allocStrategy;
        this.outStrategy = outStrategy;
        this.partitionTable.add(new Partition(partNum++,0,totalMemory));
    }

    /**
     * 设置分配策略
     * @param allocStrategy
     */
    public void setAllocStrategy(AllocStrategy allocStrategy){
        this.allocStrategy = allocStrategy;
    }

    public Process getProcess(String processName){
        if(processTable.isEmpty()){
            return null;
        }else{
            return processTable.get(processName);
        }
    }
    /**
     *
     * 添加进程
     * @param process
     */
    public void addProcess(Process process){
        processTable.put(process.processName,process);
    }

    /**
     * 为请求分配内存
     * @param request
     * @return
     */
    public boolean allocateMemeory(Request request){
        Process process = processTable.get(request.processName); // 根据进程名查找进程
        if (process == null) {
            // 进程不存在，给出错误提示
            System.out.println("错误：进程 " + request.processName + " 不存在！");
            return false;
        }

        Segment segment = process.getSegment(request.segNum); // 根据段号查找段

        if (segment == null) {
            // 段不存在，给出错误提示
            System.out.println("错误：进程 " + request.processName + " 中不存在段号 " + request.segNum);
            return false;
        }

        Partition freePart = choseFreePart(segment); // 根据分配策略选择合适的空闲块

        while(freePart == null){    // 如果没有合适的空闲块
            partNum = compact();    // 执行内存紧缩
            Partition newFreePart = partitionTable.get(partitionTable.size() - 1);  // 获取紧缩后分区说明表最后一个元素
            if(newFreePart.state || newFreePart.size < segment.size){   // 如果紧缩后没有空闲块或者空闲大小还是不够,根据淘汰策略选择淘汰算法，淘汰段
                switch (outStrategy){
                    case FIFO :
                        System.out.println("空闲空间不足，尝试使用FIFO算法淘汰段");
                        outSegmentByFIFO(); //使用FIFO算法淘汰段
                        break;
                    default:
                        System.out.println("空闲空间不足，尝试使用LFU算法淘汰段");
                        outSegmentByLFU();  // 使用LFU算法淘汰段
                        break;
                }
                freePart = choseFreePart(segment);  // 再次根据分配策略选择合适的空闲块
            }else{
                freePart = newFreePart;     // 找到了合适的空闲块，跳出循环
                break;
            }
        }

        boolean flag = allocatePartition(freePart, process, segment);   // 执行分配内存操作，修改分区说明表、段表
        if(flag){
            System.out.println("分配成功");
            // 分配成功，将其加入请求集合中
            requestList.add(requestCount++,request);

            return true;
        }else{
            System.out.println("分配失败");
            return false;
        }

    }

    /**
     * 使用FIFO算法淘汰最先进入的段
     */
    private void outSegmentByFIFO() {
        if(requestList.isEmpty()){  // 如果内存空间全部空闲，那么提示内存为空
            System.out.println("内存为空");
            return;
        }
        Request request = requestList.get(0);   // 获取第一个进入内存的段的请求信息
        Process process = processTable.get(request.processName);
        Segment segment = process.getSegment(request.segNum);   // 获取第一个进入内存的段
        System.out.println("淘汰段："+request.processName+"进程的"+request.segNum+"号段");
        freeSegment(request.processName, segment);  // 回收第一个进入内存的段
    }

    /**
     * 分配内存-修改分区说明表、段表
     * @param chosenPartition
     * @param process
     * @param segment
     * @return
     */
    private boolean allocatePartition(Partition chosenPartition,Process process,Segment segment){
        for (int i = 0; i < partitionTable.size(); i++) {   // 遍历分区说明表
            Partition partition = partitionTable.get(i);
            if(partition.partNum == chosenPartition.partNum){ // 找到通过分配算法查找到的合适的内存块
                if(partition.size > segment.size){
                    // 空闲块的大小大于需求的大小，将内存块分割为两个部分
                    Partition afterPart = new Partition(partNum++, partition.start + segment.size, partition.size - segment.size);
                    partitionTable.add(i+1,afterPart);  // 将后面的空闲块加入分区说明表中
                }
                // 修改分区说明表,将分区与段关联起来
                partition.processName = process.processName;
                partition.size = segment.size;
                partition.state = true;
                partition.segNum = segment.segNum;

                // 修改段表
                segment.state = true;
                segment.start = partition.start;
                segment.visit();
                return true;
            }
        }
        return false;
    }

    /**
     * 根据分配策略，寻找合适的空闲块
     * @param segment
     * @return
     */
    private Partition choseFreePart(Segment segment) {
        Partition freePart = null;
        switch(allocStrategy){
            case FIRST_FIT :{
                freePart = findFirstPart(segment);  // 调用最先适应法，寻找第一个合适的空闲块
                break;
            }
            case BEST_FIT:{
                freePart = findBestPart(segment);   // 调用最佳适应法，寻找最小的合适的空闲块
                break;
            }
            case WORST_FIT:{
                freePart = findWorstPart(segment);  // 调用最坏适应法，寻找最大的合适的空闲块
                break;
            }
            default:{
                freePart = findFirstPart(segment);  // 默认情况，调用最先适应法
            }
        }
        return freePart;
    }

    /**
     * 使用LFU算法，淘汰内存中访问次数最少的段
     */
    private void outSegmentByLFU() {
        Segment minVisitSeg = null; // 初始化访问次数最少的段
        String minVisitProcessName = null;

        for (Partition partition : partitionTable) {    // 遍历分区说明表
            if(partition.state){    // 如果当前内存块被占用
                Process process = processTable.get(partition.processName);
                Segment segment = process.getSegment(partition.segNum); // 获取占用该内存块的段
                if(segment.state){
                    if(minVisitSeg == null || minVisitSeg.visitCount > segment.visitCount){
                        minVisitSeg = segment;  // 如果当前段的访问次数小于minVisitSeg段的访问次数，那么将当前段赋给minVisitSeg
                        minVisitProcessName = process.processName;
                    }
                }
            }

        }

        if(minVisitSeg != null){ // 找到了访问次数最少的段，将这个段回收
            System.out.println("淘汰段："+minVisitProcessName+"进程的"+minVisitSeg.segNum+"号段");
            freeSegment(minVisitProcessName,minVisitSeg);

        }
    }

    /**
     * 内存紧缩
     */
    public int compact() {
        int addr = 0;           // 从内存的0地址开始
        int partCode = 0;
        List<Partition> newPartTable = new ArrayList<>();   // 创建一个新的分区说明表

        // 将被占用的内存块向前移
        for (Partition partition : partitionTable) {
            if(partition.state){    // 如果内存块被占用，就将其向前移
                Partition np = new Partition(partCode++,addr,partition.size);
                np.state = true;
                np.segNum = partition.segNum;               // 将内存块的内容拷贝到新内存块中
                np.processName = partition.processName;

                Process process = getProcess(partition.processName);
                Segment segment = process.getSegment(partition.segNum);
                segment.start = addr; // 修改段表

                newPartTable.add(np); // 将新的内存块加入新的分区说明表中
                addr += partition.size;
            }
        }

        // 将剩下的所有空闲块合并为一个大的空闲块
        if(addr < totalMemory){
            newPartTable.add(new Partition(partCode++,addr,totalMemory - addr));
        }

        partitionTable = newPartTable;  // 将新的分区说明表赋给partitionTable
        System.out.println("内存紧缩完成");
        displayMemoryStatus();          // 展示紧缩后内存空间的状态
        return partCode;
    }

    /**
     * 最坏适应法，寻找最大的合适的空闲块
     * @param segment
     * @return
     */
    private Partition findWorstPart(Segment segment) {
        Partition worstPart = null;
        for (Partition partition : partitionTable) {
            if(!partition.state){
                if(partition.size >= segment.size){
                    if(worstPart == null || worstPart.size < partition.size){
                        worstPart = partition;
                    }
                }
            }
        }
        return worstPart;
    }

    /**
     * 最佳适应法，寻找大小最适合的空闲块
     * @param segment
     * @return
     */
    private Partition findBestPart(Segment segment) {
        Partition bestPart = null;
        for (Partition partition : partitionTable) {
            if(!partition.state){
                if(partition.size >= segment.size){
                    if(bestPart == null || bestPart.size > partition.size){
                        bestPart = partition;
                    }
                }
            }
        }
        return bestPart;
    }

    /**
     * 最先适应法，寻找第一个合适的空闲块
     * @param segment
     * @return
     */
    private Partition findFirstPart(Segment segment) {
        for (Partition partition : partitionTable) {
            if(!partition.state){
                if(partition.size >= segment.size){
                    return partition;
                }
            }
        }
        return null;
    }


    /**
     * 回收进程processName中的段segment
     * @param processName
     * @param segment
     * @return
     */
    public boolean freeSegment(String processName,Segment segment){
        for (int i = 0; i < partitionTable.size(); i++) {
            Partition partition = partitionTable.get(i);
            if(partition.processName != null && partition.processName.equals(processName)){
                if(partition.segNum == segment.segNum){ // 找到要回收的段所占用的内存块

                    if(!segment.state){ // 如果段在外存中，那么提示重复回收
                        System.out.println("重复回收进程"+processName+"的"+segment.segNum+"段");
                        return false;
                    }
                    // 修改分区表中信息，解除内存块与段的关联
                    partition.state = false;
                    partition.segNum = -1;
                    partition.processName = null;
                    // 修改段表中的信息，将visitCount 赋为0
                    segment.state = false;
                    segment.visitCount = 0;
                    segment.start = -1;


                    // 合并空闲区
                    int preIndex = mergeWithPre(i);
                    mergeWithAfter(preIndex);


                    for (int j = 0; j < requestList.size(); j++) {
                        Request request = requestList.get(j); // 遍历请求集合，找到要回收的段对应的request
                        if(request.processName.equals(processName) && request.segNum == segment.segNum){
                            requestList.remove(j);  // 将要回收的段对应的request删除
                            requestCount--;
                        }
                    }

                    return true;
                }
            }
        }

        System.out.println("内存中未找到进程"+processName+"的"+segment.segNum+"段");
        return false;
    }

    /**
     * 与后面的空闲块合并
     * @param index
     */
    private int mergeWithAfter(int index) {
        while(index < partitionTable.size() - 1){
            Partition current = partitionTable.get(index);
            Partition next = partitionTable.get(index + 1);

            if(!current.state && !next.state){
                current.size += next.size;
                partitionTable.remove(index+1);
                index++;
            }else{
                break;
            }
        }
        return index;
    }

    /**
     * 与前面的空闲块合并
     * @param index
     */
    private int mergeWithPre(int index) {
        while(index > 0){
            Partition current = partitionTable.get(index);
            Partition pre = partitionTable.get(index - 1);

            if(!current.state && !pre.state){
                pre.size += current.size;
                partitionTable.remove(index);
                index--;
            }else{
                break;
            }
        }
        return index;
    }

    /**
     * 显示内存状态
     */
    public void displayMemoryStatus(){
        System.out.println("\n当前内存状态（总内存：" + totalMemory + " KB）:");
        for (Partition partition : partitionTable) {
            System.out.println(partition);
        }
        System.out.println();
    }

    public double getMemoryUtilization(){
        double usedMem = 0;
        for (Partition partition : partitionTable) {
            if(partition.state){
                usedMem += partition.size;
            }
        }

        return (double) usedMem / totalMemory;
    }
}
