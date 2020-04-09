import problems.ColumnName;
import problems.LoadBalancer;

public class Main {

	public static void main(String[] args) {
		System.out.println("xx");
		String columnName = ColumnName.getColumnName(26);
		System.out.println(columnName);
		int[] nums1 = {1, 3, 4, 2, 2, 2, 1, 1, 2}, nums2 = {1,1,1,1,1,1};
		System.out.println(LoadBalancer.isBalancable(nums1));
		System.out.println(LoadBalancer.isBalancable(nums2));
	}

}
