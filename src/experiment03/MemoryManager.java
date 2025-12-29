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
    // public List<FreePart> freePartList = new ArrayList<>();


    public MemoryManager(int totalMemory,AllocStrategy allocStrategy){
        this.totalMemory = totalMemory;
        this.allocStrategy = allocStrategy;
        this.partitionTable.add(new Partition(partNum++,0,totalMemory));
        // this.freePartList.add(new FreePart(partNum++,0,totalMemory));
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
        Process process = processTable.get(request.processName);
        if (process == null) {
            System.out.println("错误：进程 " + request.processName + " 不存在！");
            return false;
        }

        Segment segment = process.getSegment(request.segNum);

        if (segment == null) {
            System.out.println("错误：进程 " + request.processName + " 中不存在段号 " + request.segNum);
            return false;
        }

        // 根据分配策略选择合适的空闲块
        Partition freePart = choseFreePart(segment);


        // 如果没有合适的空闲块
        while(freePart == null){
            partNum = compact();
            Partition newFreePart = partitionTable.get(partitionTable.size() - 1);
            if(newFreePart.state || newFreePart.size < segment.size){
                // 如果紧缩后没有空闲块或者空闲大小还是不够
                System.out.println("空闲空间不足，尝试使用LRU近似方法淘汰段");
                outSegment();
                freePart = choseFreePart(segment);
            }else{
                freePart = newFreePart;
                break;
            }
        }

        boolean flag = allocatePartition(freePart, process, segment);
        if(flag){
            System.out.println("分配成功");
            return true;
        }else{
            System.out.println("分配失败");
            return false;
        }

    }

    /**
     * 分配内存-修改分区说明表、段表
     * @param chosenPartition
     * @param process
     * @param segment
     * @return
     */
    private boolean allocatePartition(Partition chosenPartition,Process process,Segment segment){
        for (int i = 0; i < partitionTable.size(); i++) {
            Partition partition = partitionTable.get(i);
            if(partition.partNum == chosenPartition.partNum){
                if(partition.size > segment.size){
                    // 将内存块分割为两个部分，将后面的块加入表中
                    Partition afterPart = new Partition(partNum++, partition.start + segment.size, partition.size - segment.size);
                    partitionTable.add(i+1,afterPart);
                }
                // 修改分区说明表
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
                freePart = findFirstPart(segment);
                break;
            }
            case BEST_FIT:{
                freePart = findBestPart(segment);
                break;
            }
            case WORST_FIT:{
                freePart = findWorstPart(segment);
                break;
            }
            default:{
                freePart = findFirstPart(segment);
            }
        }
        return freePart;
    }

    /**
     * 使用LRU近似算法，淘汰内存中访问次数最少的段
     */
    private void outSegment() {
        Segment minVisitSeg = null;
        String minVisitProcessName = null;

        for (Partition partition : partitionTable) {
            if(partition.state){
                Process process = processTable.get(partition.processName);
                Segment segment = process.getSegment(partition.segNum);
                if(segment.state){
                    if(minVisitSeg == null || minVisitSeg.visitCount > segment.visitCount){
                        minVisitSeg = segment;
                        minVisitProcessName = process.processName;
                    }
                }
            }

        }

        if(minVisitSeg != null){
            System.out.println("淘汰段："+minVisitProcessName+"进程的"+minVisitSeg.segNum+"号段");
            freeSegment(minVisitProcessName,minVisitSeg);

        }
    }

    /**
     * 内存紧缩
     */
    private int compact() {
        int addr = 0;
        int partCode = 0;
        List<Partition> newPartTable = new ArrayList<>();

        // 将被占用的内存块向前移
        for (Partition partition : partitionTable) {
            if(partition.state){
                Partition np = new Partition(partCode++,addr,partition.size);
                np.state = true;
                np.segNum = partition.segNum;
                np.processName = partition.processName;

                Process process = getProcess(partition.processName);
                Segment segment = process.getSegment(partition.segNum);
                segment.start = addr;

                newPartTable.add(np);
                addr += partition.size;
            }
        }

        // 将剩下的所有空闲块合并为一个大的空闲块
        if(addr < totalMemory){
            newPartTable.add(new Partition(partCode++,addr,totalMemory - addr));
        }

        partitionTable = newPartTable;
        System.out.println("内存紧缩完成");
        displayMemoryStatus();
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
                if(partition.segNum == segment.segNum){
                    // 找到进程对应的段
                    // 如果段在外存中，那么提示重复回收
                    if(!segment.state){
                        System.out.println("重复回收进程"+processName+"的"+segment.segNum+"段");
                        return false;
                    }
                    // 修改分区表中信息
                    partition.state = false;
                    partition.segNum = -1;
                    partition.processName = null;
                    segment.state = false;

                    // 合并空闲区
                    mergeWithPre(i);
                    mergeWithAfter(i);
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
    private void mergeWithAfter(int index) {
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
    }

    /**
     * 与前面的空闲块合并
     * @param index
     */
    private void mergeWithPre(int index) {
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
