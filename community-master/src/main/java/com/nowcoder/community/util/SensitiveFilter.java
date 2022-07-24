package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    //logger打印日志
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    //注解表示这是一个初始化方法，创建实例后，方法执行。服务启动，就创建了实例，就执行init
    @PostConstruct
    public void init() {
        try (
                //这句代码表示从target目录的class文件夹读取文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            //读取缓冲流每一行的敏感词
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }

    }
    // 将一个敏感词添加到前缀树中  注意keyword可能包含多个字符
    private void addKeyword(String keyword) {
     TrieNode tempNode=rootNode;
        for(int i=0;i<keyword.length();i++)
        {
           char c= keyword.charAt(i);
          if(tempNode.getSubNode(c)==null)
          {
              TrieNode node=new TrieNode();
              tempNode.addSubNode(c,node);
          }
            // 指向子节点,进入下一轮循环
          tempNode=tempNode.getSubNode(c);
          //结束条件
            if(i==keyword.length()-1)
                tempNode.isKeywordEnd=true;

        }
    }

    /*public String filter(String text) {
        int begin=0;
        int end=0;
        TrieNode tempNode=rootNode;
        StringBuffer sb=new StringBuffer();

        while(end<text.length())
        {
            char c=text.charAt(begin);
            if(isSymbol(c))
            {
                if(tempNode==rootNode)
                {
                    end++;
                    begin++;
                }
                else
                {
                    end++;
                }
                sb.append(c);
            }
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null)
            {
                tempNode=rootNode;
                sb.append(c);
            }
            else if (tempNode.isKeywordEnd==true)
            {
                sb.append(StringUtils.substring(begin,end))
            }
            else {

            }
        }


    }*/


    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1 字典树指针
        TrieNode tempNode = rootNode;
        // 指针2  过滤文本指针
        int begin = 0;
        // 指针3  过滤文本指针
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (begin< text.length()) {
            char c = text.charAt(position);

            // 字符为符号情况
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                   position= ++begin;
                   continue;
                }

               if(position+1==text.length())
               {
                   sb.append(c);
                   position= ++begin;
                   tempNode = rootNode;
                   continue;
               }
                // 无论符号在开头或中间,指针3都向下走一步
               position++; //这部分（begin-end）只要不 begin++; 是肯定要被遍历的 所以不用 sb.append(c);
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                if(position+1==text.length())
                {
                    sb.append(text.charAt(begin));
                    begin++;
                    position=begin;
                    tempNode = rootNode;
                    continue;
                }
               position++;
            }
        }

        return sb.toString();
    }


    // 判断是否为符号
    private boolean isSymbol(Character c) {
        //  isAsciiAlphanumeric(普通字符返回true)           0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
