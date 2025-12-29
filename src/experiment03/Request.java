package experiment03;

/**
 * 请求项
 */
public class Request {
    public String processName;  // 请求的进程名
    public int segNum;          // 进程中的段号
    public int size;            // 段的大小

    public Request(String processName,int segNum,int size) {
        this.processName = processName;
        this.segNum = segNum;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("内存请求：进程 %s ,段号 %d ,段的大小 %d ", processName, segNum, size);
    }
}
