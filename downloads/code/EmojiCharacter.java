package unicode;

public class EmojiCharacter {
    
    // high offset
    public static final int W1 = Integer.valueOf("D7C0",16);
    
    // low offset
    public static final int W2 = Integer.valueOf("DC00",16);
    
    public static void main(String[] args) {
        // 🌀的二进制字符串表示
        String doughnutBinary = Integer.toBinaryString(Integer.valueOf("1F300",16));
        // 拆成高低 10 位表示
        String highSetsBinary = doughnutBinary.substring(0, doughnutBinary.length() - 10);
        String lowSetsBinary = doughnutBinary.substring(doughnutBinary.length() - 10, doughnutBinary.length());
        System.out.println(highSetsBinary);     //    1111100
        System.out.println(lowSetsBinary);      //    1100000000
        
        // 分别与偏移量相加，得到两个编码值
        String highSetsHexFixed = Integer.toHexString(W1 + Integer.valueOf(highSetsBinary, 2));
        String lowSetsHexFixed = Integer.toHexString(W2 + Integer.valueOf(lowSetsBinary, 2));
        System.out.println(highSetsHexFixed);   //    d83c
        System.out.println(lowSetsHexFixed);    //    df00
        
        // 拼接这两个编码值，还原字符表示
        char highChar = (char)Integer.valueOf(highSetsHexFixed, 16).intValue();
        char lowChar = (char)Integer.valueOf(lowSetsHexFixed, 16).intValue();
        System.out.println(highChar);           //    ?     
        System.out.println(lowChar);            //    ?
        System.out.println(highChar + "" + lowChar);    //    🌀
    }
}
