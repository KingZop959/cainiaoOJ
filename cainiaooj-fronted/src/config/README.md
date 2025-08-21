# 编程语言配置

## 概述
本目录包含项目中编程语言相关的配置文件，用于统一管理支持的编程语言。

## 文件说明

### languages.ts
定义了项目支持的编程语言配置，包括：
- 语言标识符
- 显示名称  
- 文件扩展名
- Monaco编辑器语言标识符

## 当前支持的语言
- Java
- C++
- C
- Python

## 如何添加新语言

1. 在 `languages.ts` 的 `SUPPORTED_LANGUAGES` 数组中添加新语言配置
2. 在 `vue.config.js` 的 Monaco 插件配置中添加对应的语言标识符
3. 在 `src/utils/codeTemplates.ts` 中添加对应的代码模板

### 示例：添加 JavaScript 支持

```typescript
// 在 languages.ts 中添加
{
  id: 'javascript',
  name: 'JavaScript',
  extension: '.js',
  monacoLanguage: 'javascript'
}
```

```javascript
// 在 vue.config.js 中添加
languages: ['java', 'cpp', 'c', 'python', 'javascript']
```

```typescript
// 在 codeTemplates.ts 中添加
{
  language: 'javascript',
  template: `function solution() {
    // 在这里编写你的代码
    
}

solution();`,
  description: 'JavaScript 基础模板'
}
```

## 使用方法

### 获取语言选项
```typescript
import { getLanguageOptions } from '@/config/languages';
const options = getLanguageOptions();
```

### 获取代码模板
```typescript
import { getCodeTemplate } from '@/utils/codeTemplates';
const template = getCodeTemplate('java');
```

### 检查语言支持
```typescript
import { isSupportedLanguage } from '@/utils/codeTemplates';
const isSupported = isSupportedLanguage('java');
```
