package problems;

public class LoadBalancer {

	public static boolean isBalancable(int[] nums) {
		int sum_all = 0;
		for(int x:nums) {
			sum_all +=x;
		}
		int leftIterator = 0;
		int rightIterator = nums.length -1;
		int sum_worker1 = nums[leftIterator];
		int sum_worker3 = nums[rightIterator];
		
		while(leftIterator<rightIterator) {
			if(sum_worker1 == sum_worker3) {
				if(sum_worker1 == sum_all-(sum_worker3*2)-nums[leftIterator+1]-nums[rightIterator-1] && rightIterator - leftIterator > 1) {
					return true;
				}
				leftIterator++;
				rightIterator--;
				sum_worker1+= nums[leftIterator];
				sum_worker3+=nums[rightIterator];
			}else if(sum_worker1 > sum_worker3) {
				rightIterator--;
				sum_worker3+=nums[rightIterator];
			}else {
				leftIterator++;
				sum_worker1+= nums[leftIterator];
			}
		}
		
		return false;
	}
}
