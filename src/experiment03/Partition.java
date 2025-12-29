package experiment03;

/**
 * 分区说明表
 */
public class Partition {
    public int partNum;         // 分区号
    public int start;           // 分区起始地址
    public int size;            // 大小
    public boolean state;      // 状态位 true表示占用
    public String processName;  // 进程名
    public int segNum;          // 段号

    public Partition(int partNum, int start, int size, boolean state, String processName, int segNum){
        this.partNum = partNum;
        this.start = start;
        this.size = size;
        this.state = state;
        this.processName = processName;
        this.segNum = segNum;
    }

    public Partition(int partNum,int start,int size){
        this.partNum = partNum;
        this.start = start;
        this.size = size;
        this.state = false;
        this.processName = null;
        this.segNum = -1;
    }

    @Override
    public String toString(){
        if(state){
            return String.format("区号 %d ,[%d-%d] 进程 %s 的 %d 段 大小 %d KB",partNum, start, start + size - 1, processName, segNum,size);
        }else{
            return String.format("区号 %d ,[%d-%d] 空闲 大小 %d KB",partNum, start, start + size - 1, size);
        }
    }
}
