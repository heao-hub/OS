package experiment03;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static MemoryManager memoryManager = null;

    public static void main(String[] args) {

        // 初始化内存管理器
        while (memoryManager == null) {
            try {
                int totalMemory = InputValidator.readPositiveInteger("请输入总内存大小(KB，必须为正整数): ");
                memoryManager = new MemoryManager(totalMemory,AllocStrategy.FIRST_FIT);
            } catch (IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
                System.out.println("请重新输入!");
            }
        }

        while(true){
            showMenu();

            int choice = InputValidator.readIntegerInRange("输入选项: ",0,8);
            // scanner.nextLine();

            switch (choice){
                case 1:
                    createProcess();
                    break;
                case 2:
                    allocateSegment();
                    break;
                case 3:
                    setAllocStrategy();
                    break;
                case 4:
                    memoryManager.displayMemoryStatus();
                    break;
                case 5:
                    System.out.printf("内存利用率: %.2f%%\n", memoryManager.getMemoryUtilization()*100);
                    break;
                case 6:
                    displayProcess();
                    break;
                case 7:
                    freeSegment();
                    break;
                case 8:
                    doTest();
                    break;
                case 0:
                    System.out.println("程序退出。");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效的选项，请重试。");
            }
            System.out.println("按回车继续...");
            scanner.nextLine();


        }


    }

    private static void freeSegment() {
        memoryManager.displayMemoryStatus();
        String processName = InputValidator.readNonEmptyString("请输入要回收的段所属的进程名称：");

        Process process = memoryManager.getProcess(processName);
        if(process == null){
            System.out.println("您输入的进程信息不存在！");
            return;
        }

        int segNum = InputValidator.readNonNegativeInteger("请输入你要回收的段的段号：");

        Segment segment = process.getSegment(segNum);
        if(segment == null){
            System.out.println("你要回收的段不存在！");
            return;
        }else if(! segment.state){
            System.out.println("你要回收的段不在内存中，无需回收！");
            return;
        }
        boolean b = memoryManager.freeSegment(processName, segment);
        if(b){
            System.out.println("回收成功！");
        }else{
            System.out.println("回收失败！");
        }
        memoryManager.displayMemoryStatus();
    }

    /**
     * 显示进程信息
     */
    private static void displayProcess() {
        String processName = InputValidator.readNonEmptyString("请输入你要查看的进程名称：");
        Process process = memoryManager.getProcess(processName);
        if(process == null){
            System.out.println("您输入的进程信息不存在！");
            return;
        }
        System.out.println(process.toString());
    }

    /**
     * 设置内存分配策略
     */
    private static void setAllocStrategy() {
        System.out.println("当前分配策略: " + memoryManager.allocStrategy);
        int strategyChoice = InputValidator.readIntegerInRange("选择分配策略 (1-最先适应, 2-最佳适应, 3-最坏适应): ",1,3);
        switch (strategyChoice){
            case 1:
                memoryManager.allocStrategy = AllocStrategy.FIRST_FIT;
                break;
            case 2:
                memoryManager.allocStrategy = AllocStrategy.BEST_FIT;
                break;
            case 3:
                memoryManager.allocStrategy = AllocStrategy.WORST_FIT;
                break;
            default:
                memoryManager.allocStrategy = AllocStrategy.FIRST_FIT;
        }
        System.out.println("当前内存分配策略为："+memoryManager.allocStrategy);
    }

    /**
     * 为段申请空间
     */
    private static void allocateSegment() {
        if(memoryManager == null){
            System.out.println("内存管理器为空，请先创建内存管理器！");
            return;
        }

        System.out.println("======申请段空间======");

        int choice = InputValidator.readIntegerInRange("请选择(1--为已存在的进程的段申请空间  2--为不存在的进程的段申请空间):",1,2);
        // scanner.nextLine();

        if(choice == 1){
            String processName = InputValidator.readNonEmptyString("请输入段所属的进程名：");

            Process process = memoryManager.getProcess(processName);

            if(process == null){
                System.out.println("当前进程不存在，创建新进程！");
                return;
            }
            System.out.println(process.toString());

            int segNum = InputValidator.readNonNegativeInteger("请选择要加入内存的段号：");
            // scanner.nextLine();

            Segment segment = process.getSegment(segNum);
            if(segment == null){
                System.out.println("你要添加的段不存在！");
                return;
            }else if(segment.state){
                System.out.println("段"+segment+"已存在于内存中，请勿重复添加！");
                return;
            }
            Request request = new Request(processName,segNum,segment.size);
            memoryManager.allocateMemeory(request);
            memoryManager.displayMemoryStatus();
        }else if(choice == 2){
            String processName = InputValidator.readNonEmptyString("请输入段所属的进程名：");

            int segNum = InputValidator.readNonNegativeInteger("请选择要加入内存的段号：");
            int size = InputValidator.readNonNegativeInteger("请输入段"+segNum+"的大小(KB，必须为正整数)：");

            List<Segment> segmentList = new ArrayList<>();
            segmentList.add(new Segment(segNum,size));
            Process process = new Process(processName,segmentList);
            memoryManager.addProcess(process);

            Request request = new Request(processName,segNum,size);
            memoryManager.allocateMemeory(request);

            memoryManager.displayMemoryStatus();
        }else{
            System.out.println("无效输入！");
        }

    }

    /**
     * 创建进程及段表
     */
    private static void createProcess() {
        if(memoryManager == null){
            System.out.println("内存管理器为空，请先创建内存管理器！");
            return;
        }

        System.out.println("======创建进程======");

        String processName = InputValidator.readNonEmptyString("请输入要创建的进程名：");

        int segCount = InputValidator.readPositiveInteger("请输入进程的段数：");

        Process process = new Process(processName,segCount);

        for(int i = 0;i < segCount;i++){
            int size = InputValidator.readNonNegativeInteger("请输入段"+i+"的大小(KB，必须为正整数)：");
            process.setSegment(i,new Segment(i,size));
        }

        memoryManager.addProcess(process);

    }


    private static void showMenu() {
        System.out.println("\n请选择操作:");
        System.out.println("1. 创建进程及段表");
        System.out.println("2. 申请段内存");
        System.out.println("3. 设置分配策略");
        System.out.println("4. 显示内存状态");
        System.out.println("5. 显示内存利用率");
        System.out.println("6. 显示进程信息");
        System.out.println("7. 回收段内存");
        System.out.println("8. 测试模式");
        System.out.println("0. 退出");
    }

    private static void doTest(){

        Process p1 = new Process("p1", 2);
        p1.setSegment(0, new Segment(0, 20));
        p1.setSegment(1, new Segment(1, 10));

        Process p2 = new Process("p2", 2);
        p2.setSegment(0, new Segment(0, 40));
        p2.setSegment(1, new Segment(1, 40));

        memoryManager.addProcess(p1);
        memoryManager.addProcess(p2);

        memoryManager.allocateMemeory(new Request("p1", 0, 20));
        memoryManager.allocateMemeory(new Request("p1", 1, 10));
        memoryManager.allocateMemeory(new Request("p2", 0, 40));

        memoryManager.displayMemoryStatus();


    }
}
