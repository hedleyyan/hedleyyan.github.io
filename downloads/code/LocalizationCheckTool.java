
package repair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多语言检查工具.
 * 使用前需要将ROOTPATH配成本地路径
 * 开发一般只需要设置1和2
 * ROOTPATH1的本地地址对应的SVN地址为： http://svn.xinggeq.com/svn/chaos/trunk/Server/Project/SFS2X/languages-AOE
 * ROOTPATH2的本地地址对应的SVN地址为： http://svn.xinggeq.com/svn/chaos/trunk/Server/Project/SFS2X/database/ChaosAge/languages
 * 
 * 若检测翻译端是否有翻译语法错误还要配3和4
 * ROOTPATH3的本地地址对应的SVN地址为：http://svn.xinggeq.com/svn/chaos/trunk/Docs/Product/Localization/Develop/Server/languages-AOE
 * ROOTPATH4的本地地址对应的SVN地址为：http://svn.xinggeq.com/svn/chaos/trunk/Docs/Product/Localization/Develop/Server/Task/languages
 * 
 * @author yandeli
 * 
 */
public class LocalizationCheckTool {

    public static final String EXT = ".properties";

    // 文件本地base路径
    public static final String ROOTPATH1 = "/Users/hedley/Elex/多语言/多语言_服务端_AOE/";
    
    public static final String ROOTPATH2 = "/Users/hedley/Elex/多语言/多语言_服务端_TASK/";

    public static final String ROOTPATH3 = "/Users/hedley/Elex/多语言/多语言_翻译端/languages-AOE/";

    public static final String ROOTPATH4 = "/Users/hedley/Elex/多语言/多语言_翻译端/Task/languages/";
    
    public static final String BASE = "en"; // 对比基准语言
    
    public static final String[] OTHERFILELISTS = {
            "ks", "cn", "en", "ru", "es", "de", "ks", "fr", "it", "jp", "nl", "pl", "pt", "tk", "tw"
    }; // Other Files，对应ROOTPATH1和3

    public static final String[] TASKFILELISTS = {
            "ks", "cn", "en", "ru", "es", "de", "ks", "fr", "it", "jp", "nl", "pl", "pt", "th", "tw"
    }; // Task Files, 对应ROOTPATH2和4

    public static final int FLAG_TASK = 0; // 任务多语言标记

    public static final int FLAG_OTHER = 1; // 其他多语言标记

    public static final int FLAG_SERVER = 2; // 服务器端标记

    public static final int FLAG_DOC = 3; // 翻译端标记
    
    

    /**
     * <pre>
     * 开发人员检测多语言是否和基准对比语言有冲突，
     * 只需要调用innerCheck(FLAG_SERVER, FLAG_OTHER)或innerCheck(FLAG_SERVER, FLAG_TASK)
     * 
     * 
     * date: 2014年5月23日
     * </pre>
     * @author yandeli
     * @param args
     */
    public static void main(String[] args) {
//        biCheck(FLAG_TASK);
//        		biCheck(FLAG_OTHER);
//        innerCheck(FLAG_SERVER, FLAG_OTHER);
//        innerCheck(FLAG_SERVER, FLAG_TASK);
        innerCheck(FLAG_DOC, FLAG_OTHER);
//        innerCheck(FLAG_DOC, FLAG_TASK);
    }

    /**
     * 服务器端或翻译端所有文件和基准文件对比检测
     * 
     * @param portFlag
     * 			端标识
     * @param langflag
     * 			语言标识
     */
    public static void innerCheck(int portFlag, int langflag) {
        String base = "";
        String[] tCodes = null;
        if (langflag == FLAG_TASK) {
            tCodes = TASKFILELISTS;
            if (portFlag == FLAG_SERVER) {
                base = ROOTPATH2;
            } else if (portFlag == FLAG_DOC) {
                base = ROOTPATH4;
            } else {
                System.err.println("YOU HAVE ENTERED WRONG PORTFLAG, PLS RECHECK!");
                return;
            }
        } else if (langflag == FLAG_OTHER) {
            tCodes = OTHERFILELISTS;
            if (portFlag == FLAG_SERVER) {
                base = ROOTPATH1;
            } else if (portFlag == FLAG_DOC) {
                base = ROOTPATH3;
            } else {
                System.err.println("YOU HAVE ENTERED WRONG PORTFLAG, PLS RECHECK!");
                return;
            }
        } else {
            System.err.println("YOU HAVE ENTERED WRONG LANGFLAG, PLS RECHECK!");
            return;
        }
        String sFile = base + BASE + EXT;
        String[] tFiles = parseToFullPath(base, tCodes);
        checkMultiTargets(sFile, tFiles);
    }

    /**
     * 两端多语言文件一一对比
     * 
     * @param flag
     * 			语言标识
     */
    public static void biCheck(int langFlag) {
        String[] sFiles = null, tFiles = null;
        if (langFlag == FLAG_OTHER) {
            sFiles = parseToFullPath(ROOTPATH3, OTHERFILELISTS);
            tFiles = parseToFullPath(ROOTPATH1, OTHERFILELISTS);
        } else if (langFlag == FLAG_TASK) {
            sFiles = parseToFullPath(ROOTPATH4, TASKFILELISTS);
            tFiles = parseToFullPath(ROOTPATH2, TASKFILELISTS);
        } else {
            System.err.println("YOU HAVE ENTERED WRONG LANGFLAG, PLS RECHECK!");
            return;
        }
        for (int i = 0; i < sFiles.length; i++) {
            checkSingleTarget(sFiles[i], tFiles[i]);
            checkSingleTarget(tFiles[i], sFiles[i]);
        }
    }

    /**
     * 单个目标文件与源文件做对比
     * 
     * @param targetFilePath
     */
    public static void checkSingleTarget(String sourceFilePath, String targetFilePath) {
        HashMap<String, String> sKeyToWizard = new HashMap<String, String>();
        HashMap<String, String> tKeyToWizard = new HashMap<String, String>();
        List<String> errInfo = new ArrayList<String>();
        sKeyToWizard = initFile(sourceFilePath);
        tKeyToWizard = initFile(targetFilePath);
        for (Map.Entry<String, String> entry : sKeyToWizard.entrySet()) {
            String key = entry.getKey();
            String sWizardStr = entry.getValue();
            String tWizardStr = tKeyToWizard.get(key);
            if (null == tWizardStr) {
                System.out.println("WARNING: File --" + targetFilePath + "--    LACK KEY  :  " + key);
                continue;
            }
            if (!sWizardStr.equals(tWizardStr)) {
                errInfo.add("ERROR: File --" + targetFilePath + "--    WRONG WIZARD WITH KEY  :  " + key);
            }
        }

        for (String s : errInfo) {
            System.err.println(s);
        }

    }

    /**
     * 多目标文件与源文件对比
     * 
     * @param targetFilePaths
     */
    public static void checkMultiTargets(String sourceFilePath, String[] targetFilePaths) {
        HashMap<String, String> sKeyToWizard = new HashMap<String, String>();
        HashMap<String, String> tKeyToWizard = new HashMap<String, String>();
        List<String> errInfo = new ArrayList<String>();
        sKeyToWizard = initFile(sourceFilePath);
        for (int i = 0; i < targetFilePaths.length; i++) {
            tKeyToWizard = initFile(targetFilePaths[i]);
            for (Map.Entry<String, String> entry : sKeyToWizard.entrySet()) {
                String key = entry.getKey();
                String sWizardStr = entry.getValue();
                String tWizardStr = tKeyToWizard.get(key);
                if (null == tWizardStr) {
                    System.out.println("WARNING: File --" + targetFilePaths[i] + "--    LACK KEY  :  " + key);
                    continue;
                }
                if (!sWizardStr.equals(tWizardStr)) {
                    errInfo.add("ERROR: File --" + targetFilePaths[i] + "--    WRONG WIZARD WITH KEY  :  " + key);
                }
            }
            tKeyToWizard.clear();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String s : errInfo) {
            System.err.println(s);
        }
    }

    /**
     * 解析key对应的字符串
     * 
     * @param string
     * @return
     */
    private static String parseWizardsStr(String string) {
        char c;
        String ret = "";
        // 若包含 '$'字符，为扩展的通配法则，单独处理
        if (string.contains("$")) {
            return _parseWizardStrExt(string);
        }
        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            if ('%' != c) {
                continue;
            }
            if (i + 1 == string.length()) {
                break;
            }
            char c2 = string.charAt(i + 1);
            // 匹配 %d %s %f
            if ('d' == c2 || 's' == c2 || 'f' == c2) {
                ret = ret + c + c2;
            }
            // 匹配%.2f
            if ('.' == c2) {
                if (i + 3 >= string.length()) {
                    break;
                }
                char c3 = string.charAt(i + 2);
                char c4 = string.charAt(i + 3);
                if ('0' <= c3 && '9' >= c3 && 'f' == c4) {
                    ret = ret + c + c2 + c3 + c4;
                }
            }
        }
        return ret;
    }

    /**
     * 解析含有'$'字符的key对应的字符串
     * 
     * @param string
     * @return
     */
    private static String _parseWizardStrExt(String string) {
        char c, e;
        String ret = "";
        HashMap<Integer, String> posToChar = new HashMap<Integer, String>();
        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            if ('%' != c) {
                continue;
            }
            int pos = 0; // '%' 后第一个 '$'的位置
            Character flag = ' ';
            String sub = "";
            for (int j = i + 1; j < string.length() - 1; j++) {
                e = string.charAt(j);
                if ('$' != e) {
                    continue;
                }
                pos = j;
                flag = string.charAt(j + 1);
                if (flag == '.') {
                    // .2f适配
                    sub = string.substring(j + 1, j + 4);
                } else {
                    sub = flag.toString();
                }
                break;
            }
            int index = Integer.parseInt(string.subSequence(i + 1, pos).toString());
            posToChar.put(index, sub);
        }
        for (int i = 1;; i++) {
            if (null == posToChar.get(i)) {
                break;
            }
            ret = ret + '%' + posToChar.get(i);
        }
        return ret;
    }
    
    /**
     * 初始化文件,返回<key,解析后的字符串>类型的Hash表
     * 
     * @param fileName
     * @return
     */
    private static HashMap<String, String> initFile(String fileName) {
        String line;
        FileReader reader = null;
        BufferedReader buffer = null;
        HashMap<String, String> keyToWizard = new HashMap<String, String>();
        String[] keyValuePair;
        try {
            reader = new FileReader(fileName);
            buffer = new BufferedReader(reader);
            // 对源文件逐行解析
            line = buffer.readLine();
            while (null != line) {
                if (line.trim().indexOf("#") == 0) {
                    line = buffer.readLine();
                    continue;
                }
                // 判断key/value分隔符是 '=' 还是 ':'
                String reg = line.indexOf("=") == -1 ? ":" : line.indexOf(":") == -1 ? "=" : line.indexOf("=") < line
                        .indexOf(":") ? "=" : ":";
                keyValuePair = line.split(reg);
                String key = keyValuePair[0].trim();
                String tmp = "";
                for (int i = 1; i < keyValuePair.length; i++) {
                    tmp += keyValuePair[i];
                }
                String wizardStr = parseWizardsStr(tmp);
                if (null != key && !"".equals(key)) {
                    keyToWizard.put(key, wizardStr);
                }
                line = buffer.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != buffer) {
                    buffer.close();
                    buffer = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return keyToWizard;
    }

    private static String[] parseToFullPath(String base, String[] targets) {
        String[] ret = new String[targets.length];
        for (int i = 0; i < targets.length; i++) {
            ret[i] = base + targets[i] + EXT;
        }
        return ret;
    }

}
