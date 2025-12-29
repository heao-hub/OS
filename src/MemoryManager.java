import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MemoryManager {
    static class MemoryBlock{
        // 内存块的起始位置
        private int start;
        // 内存块的大小
        private int size;
        // 内存块是否被占用
        private boolean isFree;
        // 进程名
        private String processName;

        public MemoryBlock(int start, int size, boolean isFree, String processName){
            this.start = start;
            this.size = size;
            this.isFree = isFree;
            this.processName = processName;
        }

        @Override
        public String toString() {
            if (isFree) {
                return String.format("[%d-%d] 空闲 %d KB", start, start + size - 1, size);
            } else {
                return String.format("[%d-%d] 进程 %s %d KB", start, start + size - 1, processName, size);
            }
        }
    }


    private List<MemoryBlock> memoryBlocks;    // 内存块集合
    private int totalMemory;                   // 总内存
    private Strategy strategy;                      // 分配策略

    // 分配策略枚举
    enum Strategy {
        FIRST_FIT,      // 最先适应
        BEST_FIT,       // 最佳适应
        WORST_FIT       // 最坏适应
    }

    public MemoryManager(int totalMemory){
        this.totalMemory = totalMemory;
        this.memoryBlocks = new ArrayList<MemoryBlock>();
        memoryBlocks.add(new MemoryBlock(0,totalMemory,true,null));
        // 默认是最先事情分配策略
        this.strategy = Strategy.FIRST_FIT;
    }

    /**
     * 设置分配策略
     * @param strategy
     */
    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }


    /**
     * 分配内存
     * @param processName
     * @param size
     * @return
     */
    public boolean manageMemory(String processName,int size){
        MemoryBlock memoryBlock = null;

        switch (strategy){
            case FIRST_FIT: //最先分配策略
                memoryBlock = findFirstBlock(size);
                break;
            case BEST_FIT:  //最佳分配策略
                memoryBlock = findBestBlock(size);
                break;
            case WORST_FIT: // 最坏分配策略
                memoryBlock = findWorstBlock(size);
                break;
        }

        if(memoryBlock == null){
            System.out.println("没有足够的连续空闲空间，无法为进程"+processName+"分配内存");
            return false;
        }

        MemoryBlock newBlock = new MemoryBlock(memoryBlock.start,size,false,processName);

        memoryBlock.start = memoryBlock.start + size;
        memoryBlock.size = memoryBlock.size - size;

        // 将新的内存块加入集合中
        int index = memoryBlocks.indexOf(memoryBlock);
        memoryBlocks.add(index,newBlock);

        System.out.println("分配内存块"+newBlock);
        return true;
    }


    /**
     * 寻找size最大的合适的内存块
     * @param size
     * @return
     */
    private MemoryBlock findWorstBlock(int size) {
        MemoryBlock worstBlock = null;
        for (MemoryBlock memoryBlock : memoryBlocks) {
            if(memoryBlock.isFree && memoryBlock.size >= size){
                if(worstBlock == null || worstBlock.size < memoryBlock.size){
                    worstBlock = memoryBlock;
                }
            }
        }
        return worstBlock;
    }

    /**
     * 寻找size最小的合适的内存块
     * @param size
     * @return
     */
    private MemoryBlock findBestBlock(int size) {
        MemoryBlock bestBlock = null;
        for (MemoryBlock memoryBlock : memoryBlocks) {
            if(memoryBlock.isFree && memoryBlock.size >= size ){
                if(bestBlock == null || bestBlock.size > memoryBlock.size){
                    bestBlock = memoryBlock;
                }
            }
        }
        return bestBlock;
    }

    /**
     * 寻找第一个合适的内存块
     * @param size
     * @return
     */
    private MemoryBlock findFirstBlock(int size) {
        for (MemoryBlock memoryBlock : memoryBlocks) {
            if(memoryBlock.isFree && memoryBlock.size >= size){
                return memoryBlock;
            }
        }
        return null;
    }

    /**
     * 回收指定进程的内存
     * @param processName 要回收的进程名
     * @return 是否回收成功
     */
    public boolean freeMemory(String processName) {
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.isFree && block.processName != null && block.processName.equals(processName)) {
                // 检查是否已回收
                if (block.isFree) {
                    System.out.println("错误：进程" + processName + "已回收，重复回收！");
                    return false;
                }
                // 标记为空闲
                block.isFree = true;
                block.processName = null;
                System.out.println("回收进程" + processName + "的内存块成功");

                // 合并相邻空闲块
                mergeWithPre(i);
                mergeWithAfter(i);
                return true;
            }
        }
        System.out.println("错误：未找到进程" + processName + "，无法回收");
        return false;
    }

    /**
     * 与后面的内存块合并
     * @param index 当前内存块索引
     */
    private void mergeWithAfter(int index) {
        if (index >= memoryBlocks.size() - 1) {
            return;
        }
        MemoryBlock current = memoryBlocks.get(index);
        MemoryBlock next = memoryBlocks.get(index + 1);
        if (current.isFree && next.isFree) {
            current.size += next.size;
            memoryBlocks.remove(index + 1);
            mergeWithAfter(index); // 继续检查新的下一个块
        }
    }

    /**
     * 与前面的内存块合并
     * @param index 当前内存块索引
     */
    private void mergeWithPre(int index) {
        if (index <= 0) {
            return;
        }
        MemoryBlock current = memoryBlocks.get(index);
        MemoryBlock prev = memoryBlocks.get(index - 1);
        if (current.isFree && prev.isFree) {
            prev.size += current.size;
            memoryBlocks.remove(index);
            mergeWithPre(index - 1); // 继续检查新的前一个块
        }
    }

    /**
     * 显示内存状态
     */
    public void displayMemoryStatus(){
        System.out.println("\n当前内存状态（总内存：" + totalMemory + " KB）:");
        for (MemoryBlock block : memoryBlocks) {
            System.out.println(block);
        }
        System.out.println();
    }

    /**
     * 获取内存利用率
     * @return
     */
    public double getMemoryUtilization() {
        int usedMemory = 0;
        for (MemoryBlock block : memoryBlocks) {
            if (!block.isFree) {
                usedMemory += block.size;
            }
        }
        return (double) usedMemory / totalMemory * 100;
    }

    /**
     * 根据输入设置分配策略
     * @param strategyChoice
     */
    public static void setStrategy(MemoryManager manager, int strategyChoice){
        switch(strategyChoice) {
            case 1:
                manager.setStrategy(Strategy.FIRST_FIT);
                break;
            case 2:
                manager.setStrategy(Strategy.BEST_FIT);
                break;
            case 3:
                manager.setStrategy(Strategy.WORST_FIT);
                break;
            default:
                System.out.println("无效选择，默认使用最先适应策略");
                manager.setStrategy(Strategy.FIRST_FIT);
        }

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        MemoryManager manager = null;

        // 初始化内存管理器
        while (manager == null) {
            try {
                System.out.print("请输入总内存大小(KB，必须为正整数): ");
                int totalMemory = scanner.nextInt();
                manager = new MemoryManager(totalMemory);
            } catch (IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
                System.out.println("请重新输入!");
            }
        }

        while (true) {
            System.out.println("\n请选择操作:");
            System.out.println("1. 分配内存");
            System.out.println("2. 回收内存");
            System.out.println("3. 设置分配策略");
            System.out.println("4. 显示内存状态");
            System.out.println("5. 显示内存利用率");
            System.out.println("6. 批量创建进程");
            System.out.println("0. 退出");
            System.out.print("输入选项: ");

            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    System.out.print("输入进程名: ");
                    String processName = scanner.next();
                    System.out.print("输入所需内存大小(KB): ");
                    int size = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("选择分配策略 (1-最先适应, 2-最佳适应, 3-最坏适应): ");
                    int strategyChoice = scanner.nextInt();
                    setStrategy(manager,strategyChoice);

                    manager.manageMemory(processName, size);
                    manager.displayMemoryStatus();
                    break;

                case 2:
                    System.out.print("输入要回收的进程名: ");
                    String nameToFree = scanner.next();
                    manager.freeMemory(nameToFree);
                    manager.displayMemoryStatus();
                    break;

                case 3:
                    System.out.println("当前分配策略: " + manager.strategy);
                    System.out.println("选择分配策略 (1-最先适应, 2-最佳适应, 3-最坏适应): ");
                    strategyChoice = scanner.nextInt();
                    setStrategy(manager,strategyChoice);
                    System.out.println("当前内存分配策略为："+manager.strategy);
                    break;

                case 4:
                    manager.displayMemoryStatus();
                    break;

                case 5:
                    System.out.printf("内存利用率: %.2f%%\n", manager.getMemoryUtilization());
                    break;

                case 6:
                    System.out.print("输入要创建的进程数量: ");
                    int numProcesses = scanner.nextInt();
                    System.out.println("选择分配策略 (1-最先适应, 2-最佳适应, 3-最坏适应): ");
                    strategyChoice = scanner.nextInt();
                    setStrategy(manager,strategyChoice);

                    for (int i = 1; i <= numProcesses; i++) {
                        String procName = "P" + i;
                        int procSize = (int)(Math.random() * 512) + 1; // 随机生成1-512KB大小的进程
                        System.out.println("\n创建进程 " + procName + " 大小: " + procSize + "KB");

                        // 尝试分配
                        boolean managed = manager.manageMemory(procName, procSize);

                        // 如果分配失败，尝试其他策略
                        if (!managed && strategyChoice != 2) { // 如果不是最佳适应，尝试最佳适应
                            System.out.println("尝试使用最佳适应策略...");
                            manager.setStrategy(Strategy.BEST_FIT);
                            managed = manager.manageMemory(procName, procSize);

                            // 如果还是失败，尝试最坏适应
                            if (!managed) {
                                System.out.println("尝试使用最坏适应策略...");
                                manager.setStrategy(Strategy.WORST_FIT);
                                managed = manager.manageMemory(procName, procSize);

                                // 如果还是失败
                                if (!managed) {
                                    System.out.println("所有策略均无法分配内存给进程 " + procName);
                                } else {
                                    System.out.println("使用最坏适应策略成功分配");
                                }
                            } else {
                                System.out.println("使用最佳适应策略成功分配");
                            }
                        }
                    }
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
}

