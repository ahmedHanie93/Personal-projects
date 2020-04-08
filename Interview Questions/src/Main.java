import Adyen.MaxNailsWithSameLength;

public class Main {

	public static void main(String[] args) {
		int[] A = {1,1,1,3,5,5,8,2,7};
		int maxNailsWithSameLength = MaxNailsWithSameLength.getMaxNailsWithSameLength(A , 5);
		System.out.println(maxNailsWithSameLength);
	}

}
