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
                System.out.print("请输入总内存大小(KB，必须为正整数): ");
                int totalMemory = scanner.nextInt();
                memoryManager = new MemoryManager(totalMemory,AllocStrategy.FIRST_FIT);
            } catch (IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
                System.out.println("请重新输入!");
            }
        }

        while(true){
            showMenu();

            int choice = scanner.nextInt();
            scanner.nextLine();

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
                    System.out.printf("内存利用率: %.2f%%\n", memoryManager.getMemoryUtilization());
                    break;
                case 6:
                    displayProcess();
                    break;
                case 7:
                    freeSegment();
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
        System.out.println("请输入要回收的段所属的进程名称：");
        String processName = scanner.nextLine();

        Process process = memoryManager.getProcess(processName);
        if(process == null){
            System.out.println("您输入的进程信息不存在！");
            return;
        }
        System.out.println("请输入你要回收的段的段号：");
        int segNum = scanner.nextInt();

        Segment segment = process.getSegment(segNum);
        if(segment == null){
            System.out.println("你要回收的段不存在！");
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
        System.out.println("请输入你要查看的进程名称：");
        String processName = scanner.nextLine();
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
        System.out.println("选择分配策略 (1-最先适应, 2-最佳适应, 3-最坏适应): ");
        int strategyChoice = scanner.nextInt();
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

        System.out.println("请选择：1--为已存在的进程的段申请空间  2--为不存在的进程的段申请空间");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if(choice == 1){
            System.out.println("请输入段所属的进程名：");
            String processName = scanner.nextLine();

            Process process = memoryManager.getProcess(processName);

            if(process == null){
                System.out.println("当前进程不存在，创建新进程！");
                return;
            }
            System.out.println(process.toString());

            System.out.println("请选择要加入内存的段号：");
            int segNum = scanner.nextInt();
            scanner.nextLine();

            Segment segment = process.getSegment(segNum);
            Request request = new Request(processName,segNum,segment.size);
            memoryManager.allocateMemeory(request);
        }else if(choice == 2){
            System.out.println("请输入要创建的进程名：");
            String processName = scanner.nextLine();

            System.out.println("请输入要加入内存的段号：");
            int segNum = scanner.nextInt();
            System.out.println("请输入段"+segNum+"的大小(单位为KB)：");
            int size = scanner.nextInt();
            List<Segment> segmentList = new ArrayList<>();
            segmentList.add(new Segment(segNum,size));
            Process process = new Process(processName,segmentList);
            memoryManager.addProcess(process);

            Request request = new Request(processName,segNum,size);
            memoryManager.allocateMemeory(request);
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

        System.out.println("请输入要创建的进程名：");
        String processName = scanner.nextLine();

        System.out.println("请输入进程的段数：");
        int segCount = scanner.nextInt();

        Process process = new Process(processName,segCount);

        for(int i = 0;i < segCount;i++){
            System.out.println("请输入段"+i+"的大小：");
            int size = scanner.nextInt();
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
        System.out.println("0. 退出");
        System.out.print("输入选项: ");
    }

    private void doTest(){
        MemoryManager mm = new MemoryManager(100, AllocStrategy.FIRST_FIT);

        Process p1 = new Process("P1", 2);
        p1.setSegment(0, new Segment(0, 20));
        p1.setSegment(1, new Segment(1, 30));

        Process p2 = new Process("P2", 1);
        p2.setSegment(0, new Segment(0, 40));

        mm.addProcess(p1);
        mm.addProcess(p2);

        mm.allocateMemeory(new Request("P1", 0, 20));
        mm.allocateMemeory(new Request("P1", 1, 30));
        mm.allocateMemeory(new Request("P2", 0, 40));

        mm.displayMemoryStatus();

        System.out.println("再次请求，触发淘汰：");
        mm.allocateMemeory(new Request("P2", 0, 40));
        mm.displayMemoryStatus();
    }
}
