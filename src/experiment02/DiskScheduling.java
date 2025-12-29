package experiment02;

import java.util.*;

public class DiskScheduling {

    static final int DISK_SIZE = 200; // 磁道范围 0~199

    /**
     * 先来先服务
     * @param diskScheduleCase
     * @return Result
     */
    public static Result fcfs(DiskScheduleCase diskScheduleCase) {
        List<Integer> order = new ArrayList<>();
        int total = 0;
        int cur = diskScheduleCase.start;

        order.add(cur);
        for (int r : diskScheduleCase.requests) {
            total += Math.abs(cur - r);
            cur = r;
            order.add(cur);
        }
        return new Result(order, total);
    }

    /**
     * 最短寻道优先
     * @param diskScheduleCase
     * @return Result
     */
    public static Result sstf(DiskScheduleCase diskScheduleCase) {
        TreeSet<Integer> set = new TreeSet<>(diskScheduleCase.requests);
        List<Integer> order = new ArrayList<>();
        int total = 0;
        int cur = diskScheduleCase.start;

        order.add(cur);

        while (!set.isEmpty()) {
            Integer lower = set.floor(cur);
            Integer higher = set.ceiling(cur);

            int next;
            if (lower == null) {
                next = higher;
            } else if (higher == null) {
                next = lower;
            } else {
                next = (cur - lower <= higher - cur) ? lower : higher;
            }

            total += Math.abs(cur - next);
            cur = next;
            order.add(cur);
            set.remove(next);
        }
        return new Result(order, total);
    }

    /**
     * 电梯算法
     * @param diskScheduleCase
     * @return
     */
    public static Result scan(DiskScheduleCase diskScheduleCase) {
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        for (int r : diskScheduleCase.requests) {
            if (r < diskScheduleCase.start)
                left.add(r);
            else
                right.add(r);
        }

        Collections.sort(left);
        Collections.sort(right);

        List<Integer> order = new ArrayList<>();
        int total = 0;
        int cur = diskScheduleCase.start;
        order.add(cur);

        if (diskScheduleCase.direction.equalsIgnoreCase("right")) {
            for (int r : right) {
                total += Math.abs(cur - r);
                cur = r;
                order.add(cur);
            }
            // 只有存在左侧请求时才反向
            if (!left.isEmpty()) {
                total += Math.abs(cur - (DISK_SIZE - 1));
                cur = DISK_SIZE - 1;
                order.add(cur);

                for (int i = left.size() - 1; i >= 0; i--) {
                    total += Math.abs(cur - left.get(i));
                    cur = left.get(i);
                    order.add(cur);
                }
            }
        } else {
            for (int i = left.size() - 1; i >= 0; i--) {
                total += Math.abs(cur - left.get(i));
                cur = left.get(i);
                order.add(cur);
            }
            if (!right.isEmpty()) {
                total += Math.abs(cur - 0);
                cur = 0;
                order.add(cur);

                for (int r : right) {
                    total += Math.abs(cur - r);
                    cur = r;
                    order.add(cur);
                }
            }
        }
        return new Result(order, total);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int choice = 0;
        while (true) {
            try {
                System.out.println("请输入要进入的模式:1-运行模式，2-测试模式 ");
                choice = sc.nextInt();
                System.out.println(choice);
                if (choice != 1 && choice != 2)
                    throw new IllegalArgumentException();
                break;
            } catch (Exception e) {
                System.out.println("输入非法，请输入整数1或者2");
                sc.nextLine();
            }
        }

        if (choice == 2) {
            // 测试用例1：请求序列为空
            List<DiskScheduleCase> diskScheduleCases = getDiskScheduleCases();
            for (DiskScheduleCase diskScheduleCase : diskScheduleCases) {
                doTest(diskScheduleCase);
            }
        }else{
            int start = -1;
            while (true) {
                try {
                    System.out.print("请输入当前磁头位置(0~199): ");
                    start = sc.nextInt();
                    if (start < 0 || start >= DISK_SIZE)
                        throw new IllegalArgumentException();
                    break;
                } catch (Exception e) {
                    System.out.println("输入非法，请输入 0~199 之间的整数");
                    sc.nextLine();
                }
            }

            String direction;
            while (true) {
                System.out.print("请输入磁头移动方向(left/right): ");
                direction = sc.next();
                if (direction.equalsIgnoreCase("left") ||
                        direction.equalsIgnoreCase("right")) {
                    break;
                }
                System.out.println("方向只能是 left 或 right");
            }

            sc.nextLine(); // 清空缓冲区
            System.out.print("请输入磁道请求序列(空格分隔): ");
            String line = sc.nextLine();

            if (line.trim().isEmpty()) {
                System.out.println("请求序列为空，总寻道数为0");
                return;
            }

            List<Integer> requests = new ArrayList<>();
            String[] tokens = line.split("\\s+");

            for (String s : tokens) {
                try {
                    int r = Integer.parseInt(s);
                    if (r < 0 || r >= DISK_SIZE) {
                        System.out.println("忽略非法磁道号: " + r);
                        continue;
                    }
                    requests.add(r);
                } catch (NumberFormatException e) {
                    System.out.println("忽略非法输入: " + s);
                }
            }

            if (requests.isEmpty()) {
                System.out.println("没有合法磁道请求，程序结束");
                return;
            }

            DiskScheduleCase diskScheduleCase = new DiskScheduleCase(start,direction,requests);
            System.out.println("\n========== 调度结果 ==========");


            print("FCFS",fcfs(diskScheduleCase));

            print("SSTF",sstf(diskScheduleCase));

            print("SCAN",scan(diskScheduleCase));
        }

        sc.close();
    }

    private static List<DiskScheduleCase> getDiskScheduleCases() {
        DiskScheduleCase diskScheduleCase1 = new DiskScheduleCase(50, "right", new ArrayList<>());

        // 测试用例2：所有请求在磁头一侧
        DiskScheduleCase diskScheduleCase2 = new DiskScheduleCase(100, "right", Arrays.asList(150, 160, 143, 170, 180, 190));

        // 测试用例3：磁头位于边界位置（0），方向向右
        DiskScheduleCase diskScheduleCase3 = new DiskScheduleCase( 0, "right", Arrays.asList(100, 150, 190));

        // 测试用例4：请求序列有越界磁道号
        DiskScheduleCase diskScheduleCase4 = new DiskScheduleCase(100, "left", Arrays.asList(100, 150, 220,66, 120));

        List<DiskScheduleCase> diskScheduleCases = new ArrayList<>();
        diskScheduleCases.add(diskScheduleCase1);
        diskScheduleCases.add(diskScheduleCase2);
        diskScheduleCases.add(diskScheduleCase3);
        diskScheduleCases.add(diskScheduleCase4);
        return diskScheduleCases;
    }

    private static void doTest(DiskScheduleCase diskScheduleCase) {
        System.out.println("\n====================");
        System.out.println("当前磁头位置: " + diskScheduleCase.start + ", 方向: " + diskScheduleCase.direction + ", 请求序列: " + diskScheduleCase.requests);
        if(diskScheduleCase.requests.size() == 0){
            System.out.println("请求序列为空，总寻道数为0");
            System.out.println("=================");
        }




        print("FCFS", fcfs(diskScheduleCase));
        print("SSTF", sstf(diskScheduleCase));
        print("SCAN", scan(diskScheduleCase));

    }

    public static void print(String name, Result r) {
        System.out.println("\n【" + name + "】");
        System.out.println("访问顺序: " + formatOrder(r.order));
        System.out.println("磁头移动总磁道数: " + r.totalMovement);
    }

    private static String formatOrder(List<Integer> order) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            sb.append(order.get(i));
            if (i != order.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }
}

class Result {
    List<Integer> order;
    int totalMovement;

    Result(List<Integer> order, int totalMovement) {
        this.order = order;
        this.totalMovement = totalMovement;
    }
}

class DiskScheduleCase {
    int start;
    String direction;
    List<Integer> requests = new ArrayList<>();

    DiskScheduleCase(int start, String direction, List<Integer> requests) {
        this.start = start;
        this.direction = direction;
        this.requests = requests;
    }
}

