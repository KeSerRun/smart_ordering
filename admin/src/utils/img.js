/**
 * 默认图片占位（内联 SVG，无外部依赖）：
 * 菜品/分类没有图片或图片 URL 加载失败时展示。
 */
export const DEFAULT_IMG =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 80 80'>" +
  "<rect width='80' height='80' fill='%23f0f2f5' rx='8'/>" +
  "<circle cx='40' cy='40' r='24' fill='none' stroke='%23c8ccd4' stroke-width='3'/>" +
  "<circle cx='40' cy='40' r='7' fill='%23c8ccd4'/></svg>"