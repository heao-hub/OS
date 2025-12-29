package experiment03;


import java.util.*;

/**
 * 进程
 */
public class Process {
    public String processName;         // 进程名
    public List<Segment>  segTable;    // 段表

    public Process(String processName, List<Segment> segTable){
        this.processName = processName;
        this.segTable = new ArrayList<>();

        this.segTable.addAll(segTable);
    }

    public Process(String processName,int segCount){
        this.processName = processName;
        this.segTable = new ArrayList<>();
        for(int i = 0; i < segCount; i++){
            this.segTable.add(null);
        }
    }


    /**
     * 修改段表
     * @param segNum
     * @param seg
     */
    public void setSegment(int index, Segment seg) {
        segTable.set(index,seg);
    }

    public Segment getSegment(int segNum) {
        for(Segment seg : segTable){
            if(segNum == seg.segNum){
                return seg;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("当前进程").append(processName).append("\n");

        for(int i = 0; i < segTable.size(); i++){
            Segment segment = segTable.get(i);
            sb.append(segment.toString()).append("\n");
        }
        return sb.toString();
    }
}
