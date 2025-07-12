package com.yupi.yuojbackendjudgeservice.judge.codesandbox.impl.Judge0API;

public enum Judge0Language {
    BASH(46, "Bash"),
    BASIC_FBC(47, "Basic"),
    C_CLANG(104, "Clang"),
    CPP_CLANG(76, "C++_Clang"),
    C_GCC(103, "C_GCC"),
    CPP_GCC(52, "C++_GCC"),
    DART(90, "Dart"),
    D_DMD(56, "D"),
    ELIXIR(57, "Elixir"),
    ERLANG_OTP(58, "Erlang"),
    EXECUTABLE(44, "Executable"),
    F_SHARP_NET_CORE_SDK(87, "F#"),
    FORTRAN_GFORTRAN(59, "Fortran"),
    GO(107, "Go"),
    GROOVY(88, "Groovy"),
    HASKELL(61, "Haskell"),
    JAVA(91, "java"),
    JAVASCRIPT(102, "JavaScript"),
    KOTLIN(111, "Kotlin"),
    LUA(64, "Lua"),
    PHP(68, "PHP"),
    PYTHON(71, "Python"),
    R(99, "R"),
    RUBY(72, "Ruby"),
    RUST(73, "Rust"),
    SCALA(81, "Scala"),
    SQL_SQLITE(82, "SQL"),
    SWIFT(83, "Swift"),
    TYPESCRIPT(101, "TypeScript");

    private final int id;
    private final String description;

    Judge0Language(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据用户输入的字符串模糊匹配枚举项
     * @param input 用户输入的字符串，例如 "python" 或 "Java"
     * @return 如果找到匹配的枚举项则返回，否则返回 null
     */
    public static Judge0Language fromDescription(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String lowerInput = input.trim().toLowerCase();
        for (Judge0Language lang : values()) {
            if (lang.description.toLowerCase().contains(lowerInput)) {
                return lang;
            }
        }
        return null;
    }
}
