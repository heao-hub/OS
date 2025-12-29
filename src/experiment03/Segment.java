package experiment03;

/**
 * 段结构
 */
public class Segment {
    public int segNum;      // 段号
    public int start;       // 起始地址
    public int size;        // 段的大小
    public int visitCount;  // 访问次数
    public boolean state;  // 状态位 true表示在内存中，false表示在外存中

    public Segment(int segNum,int start,int size,int visitCount,boolean state){
        this.segNum = segNum;
        this.start = start;
        this.size = size;
        this.visitCount = visitCount;
        this.state = state;
    }

    public Segment(int segNum,int size){
        this.segNum = segNum;
        this.size = size;
        this.start = 0;
        this.visitCount = 0;
        this.state = false;
    }

    public void visit(){
        this.visitCount++;
    }

    @Override
    public String toString(){
        if(this.state){
            return String.format("段号： %d ，起始地址： %d ，大小： %d ,访问次数： %d ,状态：在内存中", segNum, start, size, visitCount);
        }else{
            return String.format("段号： %d ，起始地址： %d ，大小： %d ,访问次数： %d ,状态：不在内存中", segNum, start, size, visitCount);
        }

    }
}
