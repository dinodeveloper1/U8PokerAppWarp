package computerpackage;

public enum ComputerTypeLevel 
{
	ComputerTypeLevelEasy(15010),
	ComputerTypeLevelMedium(15011),
	ComputerTypeLevelHard(15011);	
	private int typeValue;
	
	ComputerTypeLevel(int value)
	{
		this.typeValue = value;
	}
	public int getComputerTypeLevel() 
	{
		return this.typeValue;	
	}
}
