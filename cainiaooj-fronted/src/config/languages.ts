/**
 * 编程语言配置
 */

export interface LanguageConfig {
  // 语言标识符（用于Monaco编辑器）
  id: string;
  // 显示名称
  name: string;
  // 文件扩展名
  extension: string;
  // Monaco编辑器语言标识符
  monacoLanguage: string;
}

/**
 * 支持的编程语言列表
 */
export const SUPPORTED_LANGUAGES: LanguageConfig[] = [
  {
    id: "java",
    name: "Java",
    extension: ".java",
    monacoLanguage: "java",
  },
  {
    id: "cpp",
    name: "C++",
    extension: ".cpp",
    monacoLanguage: "cpp",
  },
  {
    id: "c",
    name: "C",
    extension: ".c",
    monacoLanguage: "c",
  },
  {
    id: "python",
    name: "Python",
    extension: ".py",
    monacoLanguage: "python",
  },
];

/**
 * 默认编程语言
 */
export const DEFAULT_LANGUAGE = "java";

/**
 * 获取语言配置
 * @param languageId 语言标识符
 * @returns 语言配置对象
 */
export function getLanguageConfig(
  languageId: string
): LanguageConfig | undefined {
  return SUPPORTED_LANGUAGES.find((lang) => lang.id === languageId);
}

/**
 * 获取所有支持的语言ID列表
 * @returns 语言ID数组
 */
export function getSupportedLanguageIds(): string[] {
  return SUPPORTED_LANGUAGES.map((lang) => lang.id);
}

/**
 * 获取所有支持的语言选项（用于下拉框）
 * @returns 包含value和label的选项数组
 */
export function getLanguageOptions(): Array<{ value: string; label: string }> {
  return SUPPORTED_LANGUAGES.map((lang) => ({
    value: lang.id,
    label: lang.name,
  }));
}
