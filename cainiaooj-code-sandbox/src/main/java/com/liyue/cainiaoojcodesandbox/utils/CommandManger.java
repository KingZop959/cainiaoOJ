package com.liyue.cainiaoojcodesandbox.utils;

import java.io.File;

public class CommandManger {
    public static String[] getCommands(String language,File codeFile){
        String userUUID = codeFile.getParentFile().getName();
        if (language.equals("java")){
            return new String[]{"java","-cp","/app"+File.separator+userUUID,"Main",};
        }else if (language.equals("python")){
            return new String[]{"python3","/app/"+userUUID+"/Solution.py"};
        }else {
            return new String[]{"/app/"+userUUID+"/Main"};
        }
    }

    public static String getComplieCommands(String language, File codeFile) {
        if (language.equals("java")){
            return String.format("javac -encoding utf-8 %s",codeFile.getAbsolutePath());
        }
        else if (language.equals("c")){
            return String.format("gcc %s -o %sMain ",codeFile.getAbsolutePath(),codeFile.getParentFile().getAbsolutePath()+File.separator);
        }else {
            return String.format("g++ %s -o %sMain ",codeFile.getAbsolutePath(),codeFile.getParentFile().getAbsolutePath()+File.separator);
        }
    }
}
