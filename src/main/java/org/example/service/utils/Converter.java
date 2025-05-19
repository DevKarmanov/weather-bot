package org.example.service.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {

    public static String convert(String text) {
        if (text == null || text.isEmpty()) return "";

        // Экранирование спецсимволов HTML (сначала, чтобы не испортить теги)
        text = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // Обработка многострочного кода ```...```
        text = replaceMultilineCodeBlocks(text);

        // Обработка однострочного кода `...`
        text = text.replaceAll("`([^`\n]+?)`", "<code>$1</code>");

        // Заголовки (#, ##, ###) — жирный + пустая строка
        text = text.replaceAll("(?m)^### (.+)$", "<b>$1</b>\n\n");
        text = text.replaceAll("(?m)^## (.+)$", "<b>$1</b>\n\n");
        text = text.replaceAll("(?m)^# (.+)$", "<b>$1</b>\n\n");

        // Жирный текст **text**
        text = replaceAllNonGreedy(text, "\\*\\*(.+?)\\*\\*", "<b>$1</b>");

        // Подчёркнутый __text__
        text = replaceAllNonGreedy(text, "__(.+?)__", "<u>$1</u>");

        // Зачёркнутый ~~text~~
        text = replaceAllNonGreedy(text, "~~(.+?)~~", "<s>$1</s>");

        // Курсив *text*
        text = replaceAllNonGreedy(text, "\\*(.+?)\\*", "<i>$1</i>");

        // Списки — преобразуем в текст с маркерами (без HTML тегов)
        text = convertUnorderedLists(text);
        text = convertOrderedLists(text);

        // Переносы строк оставляем как есть (\n)
        // (входящий текст уже разбит на строки, замену <br> убрали)

        return text.trim();
    }

    private static String replaceMultilineCodeBlocks(String text) {
        // ```code```
        Pattern p = Pattern.compile("```([\\s\\S]+?)```");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String codeContent = m.group(1);
            // Не экранируем теги внутри кода (заменили до этого), просто вставляем как есть
            codeContent = codeContent.replaceAll("<br>", "\n");
            String replacement = "<pre>" + codeContent + "</pre>";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String replaceAllNonGreedy(String text, String regex, String replacement) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String inner = m.group(1);
            m.appendReplacement(sb, replacement.replace("$1", inner));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String convertUnorderedLists(String text) {
        // Превратим строки, начинающиеся с "- ", "* ", "+ " в строки с маркером "• "
        String[] lines = text.split("\\r?\\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (line.matches("^\\s*[-*+]\\s+.+")) {
                String item = line.replaceFirst("^\\s*[-*+]\\s+", "• ");
                result.append(item).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private static String convertOrderedLists(String text) {
        // Оставим нумерованные списки как есть (они уже в виде "1. ", "2. " и т.п.)
        // Просто убедимся, что после них перенос строки есть
        String[] lines = text.split("\\r?\\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (line.matches("^\\s*\\d+\\.\\s+.+")) {
                result.append(line.trim()).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }
}
