/**
 * 编程语言代码模板
 */

import { LanguageConfig, SUPPORTED_LANGUAGES } from "@/config/languages";

/**
 * 代码模板接口
 */
export interface CodeTemplate {
  language: string;
  template: string;
  description: string;
}

/**
 * 各语言的基础代码模板
 */
export const CODE_TEMPLATES: CodeTemplate[] = [
  {
    language: "java",
    template: `public class Main {
    public static void main(String[] args) {
        // 在这里编写你的代码
        
    }
}`,
    description: "Java 基础模板",
  },
  {
    language: "cpp",
    template: `#include <iostream>
#include <vector>
#include <string>
using namespace std;

int main() {
    // 在这里编写你的代码
    
    return 0;
}`,
    description: "C++ 基础模板",
  },
  {
    language: "c",
    template: `#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    // 在这里编写你的代码
    
    return 0;
}`,
    description: "C 基础模板",
  },
  {
    language: "python",
    template: `def solution():
    # 在这里编写你的代码
    pass

if __name__ == "__main__":
    solution()`,
    description: "Python 基础模板",
  },
];

/**
 * 获取指定语言的代码模板
 * @param language 编程语言标识符
 * @returns 代码模板字符串
 */
export function getCodeTemplate(language: string): string {
  const template = CODE_TEMPLATES.find((t) => t.language === language);
  return template ? template.template : "";
}

/**
 * 获取所有代码模板
 * @returns 所有代码模板数组
 */
export function getAllCodeTemplates(): CodeTemplate[] {
  return CODE_TEMPLATES;
}

/**
 * 判断是否支持指定语言
 * @param language 编程语言标识符
 * @returns 是否支持
 */
export function isSupportedLanguage(language: string): boolean {
  return SUPPORTED_LANGUAGES.some((lang) => lang.id === language);
}
