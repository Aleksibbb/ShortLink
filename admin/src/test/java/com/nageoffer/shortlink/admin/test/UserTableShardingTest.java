package com.nageoffer.shortlink.admin.test;

import java.util.ArrayList;
import java.util.List;

public class UserTableShardingTest {

    public static final String SQL_USER = "CREATE TABLE `t_user_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `username` varchar(256) NOT NULL DEFAULT '' COMMENT '用户名',\n" +
            "  `password` varchar(512) NOT NULL COMMENT '密码',\n" +
            "  `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',\n" +
            "  `phone` varchar(128) DEFAULT NULL COMMENT '手机号',\n" +
            "  `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',\n" +
            "  `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_username` (`username`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1762831593791193091 DEFAULT CHARSET=utf8mb4;";

    public static final String SQL_LINK = "CREATE TABLE `t_link_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `domain` varchar(128) DEFAULT NULL COMMENT '域名',\n" +
            "  `short_uri` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '短链接',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "  `origin_url` varchar(1024) DEFAULT NULL COMMENT '原始链接',\n" +
            "  `click_num` int(11) DEFAULT '0' COMMENT '点击量',\n" +
            "  `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',\n" +
            "  `favicon` varchar(256) DEFAULT NULL COMMENT '网站图标',\n" +
            "  `enable_status` tinyint(1) DEFAULT NULL COMMENT '启用标识 0：启用 1：未启用',\n" +
            "  `created_type` tinyint(1) DEFAULT NULL COMMENT '创建类型 0：接口创建 1：控制台创建',\n" +
            "  `valid_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：用户自定义',\n" +
            "  `valid_date` datetime DEFAULT NULL COMMENT '有效期',\n" +
            "  `describe` varchar(1024) DEFAULT NULL COMMENT '描述',\n" +
            "  `total_pv` int(11) DEFAULT NULL COMMENT '历史PV',\n" +
            "  `total_uv` int(11) DEFAULT NULL COMMENT '历史UV',\n" +
            "  `total_uip` int(11) DEFAULT NULL COMMENT '历史UIP',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1763546121545846786 DEFAULT CHARSET=utf8mb4;";


    public static final String SQL_GROUP = "CREATE TABLE `t_group_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `gid` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '分组标识',\n" +
            "  `name` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '分组名称',\n" +
            "  `username` varchar(256) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '创建分组名称',\n" +
            "  `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除  1：删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1763843724682690562 DEFAULT CHARSET=utf8;";

    public static final String SQL_LINK_GOTO = "CREATE TABLE `t_link_goto_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

    public static final String SQL_LINK_STATS_TODAY = "CREATE TABLE `t_link_stats_today_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',\n" +
            "  `date` date DEFAULT NULL COMMENT '日期',\n" +
            "  `today_pv` int(11) DEFAULT '0' COMMENT '今日PV',\n" +
            "  `today_uv` int(11) DEFAULT '0' COMMENT '今日UV',\n" +
            "  `today_uip` int(11) DEFAULT '0' COMMENT '今日IP数',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`,`gid`,`date`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;";
    public static void main(String[] args) {
//        for (int i = 0; i < 16; i++) {
//            System.out.printf((SQL_LINK_STATS_TODAY) + "%n", i);
//        }

        sort();

//        int[] nums = {1,2,3,5};
//        IntStream.of(nums).boxed().sorted((a, b) -> b - a).mapToInt(Integer::intValue).toArray();

//        int[][] matrix = {{1,2,3,4},{5,6,7,8},{9,10,11,12}};
//        List<Integer> res = spiralOrder(matrix);
//        System.out.println(res);

    }

    public static List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        int i = 0; int j = 0;
        int loop = 1;
        int start = 0;
        int m = matrix.length; int n = matrix[0].length;
        while(loop <= (Math.min(m, n) / 2)){
            for(j = start; j < n - loop; j++){
                result.add(matrix[start][j]);
            }
            for(i = start; i < m - loop; i++){
                result.add(matrix[i][j]);
            }
            for( ; j >= loop; j--){
                result.add(matrix[i][j]);
            }
            for( ; i >= loop; i--){
                result.add(matrix[i][j]);
            }
            loop++;
            start++;
        }
        if(m <= n){
            for( j = start; j <= n - loop; j++){
                result.add(matrix[start][j]);
            }
        }else{
            for( i = start; i <= m - loop; i++){
                result.add(matrix[i][start]);
            }
        }
        return result;
    }

    public static void sort(){
        int[] arr1 = {2, 3, 8, 1, 4, 9, 10, 7, 16, 14};
        int[] arr2 = {2, 3, 8, 1, 4, 9, 10, 7, 16, 14};
        heapSort(arr1, 10);
        for (int i = 0; i < arr1.length; i++) {
            System.out.println(arr1[i]);
        }
        quickSort(arr2, 0, arr2.length - 1);
        for (int i = 0; i < arr2.length; i++) {
            System.out.println(arr2[i]);
        }
    }

    /**
     * 维护堆的性质
     * @param arr 存储堆元素的数组
     * @param n   数组长度
     * @param i   待维护节点的下标
     */
    public static void heapify(int[] arr, int n, int i){
        int largest = i;
        int leftSon = 2*i + 1;
        int rightSon = 2*i + 2;
        if(leftSon < n && arr[largest] < arr[leftSon]) largest = leftSon;
        if(rightSon < n && arr[largest] < arr[rightSon]) largest = rightSon;
        if(largest != i){
            swap(arr, i, largest);
            heapify(arr, n, largest);
        }
    }
    /**
     * 堆排序
     * @param arr 存储堆元素的数组
     * @param n   数组长度
     */
    public static void heapSort(int[] arr, int n){
        //1. 建堆
        for(int i = n/2 - 1; i >= 0; i--){
            heapify(arr, n, i);
        }
        //2. 堆排序
        for(int i = n - 1; i > 0; i--){
            swap(arr, i, 0);
            heapify(arr, i, 0);        // 这里传入的数组元素长度必须为i，因为在排序过程中，需要维护的数组长度是变小的
        }
    }

    public static void swap(int[] nums, int i, int j) {
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }

    /**
     * 快速排序
     * @param arr：待排序数组
     * @param start：起始下标
     * @param end：终点下标
     */
    public static void quickSort(int[] arr, int start, int end){	//start = 0, end = arr.length - 1
        //1. 递归的终止条件
        if(start >= end)	return;
        //2. 选定中心轴
        int left = start, right = end;
        int pivot = arr[left];
        while(left < right){
            //2.1 右指针 >= pivot
            while(left < right && arr[right] >= pivot)	right--;
            if(left < right){	// 证明此时 arr[right] < pivot
                arr[left] = arr[right];
                left++;
            }
            //2.2 左指针 <= pivot
            while(left < right && arr[left] <= pivot)	left++;
            if(left < right){	// 证明此时 arr[left] > pivot
                arr[right] = arr[left];
                right--;
            }
        }
        arr[left] = pivot;
        quickSort(arr, start, left - 1);
        quickSort(arr, left + 1, end);
    }
}
