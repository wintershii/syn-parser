package parser;

import java.util.*;

/**
 * 用来处理输入的文法，将文法中的终结符和非终结符分别存储，检测直接左递归和左公因子
 * 求First集和Follow集
 */
public class Grammar {
    private int num;
    private Node[] analyse = new Node[100];
    private Set<Character>[] firstSet = new HashSet[100];//first集
    private Set<Character>[] followSet = new HashSet[100];//follow集
    private List<Character> nonBlankTerminal = new ArrayList<Character>();//去空终结符
    private List<Character> terminal = new ArrayList<Character>();//终结符
    private List<Character> nonTerminal = new ArrayList<Character>();//非终结符


    public boolean isNotSymbol(char c) {
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        return false;
    }
    //获得在终结符集合中的下标
    public int getIndex(char target) {
        for (int i = 0; i < nonTerminal.size(); ++i) {
            if (target == nonTerminal.get(i)) {
                return i;
            }
        }

        return -1;
    }

    public int getNonBankIndex(char target) {
        for (int i = 0; i < nonBlankTerminal.size(); ++i) {
            if (target == nonBlankTerminal.get(i)) {
                return i;
            }
        }
        return -1;
    }

    //生成指定非终结符的first集
    public void getFirst(char target) {
        int tag = 0;
        int flag = 0;
        for (int i = 0; i < num; ++i) {
            if (analyse[i].left == target) {//匹配产生式左部
                if ( !isNotSymbol(analyse[i].right.charAt(0))) {//终结符直接加入first
                    firstSet[getIndex(target)].add(analyse[i].right.charAt(0));
                } else {
                    for (int j = 0; j < analyse[i].right.length(); ++j) {
                        if (!isNotSymbol(analyse[i].right.charAt(j))) {
                            firstSet[getIndex(target)].add(analyse[i].right.charAt(j));
                            break;
                        }
                        getFirst(analyse[i].right.charAt(j));//递归调用

                        for (char c : firstSet[getIndex(analyse[i].right.charAt(j))]) {
                            if (c == '$') {
                                flag = 1;
                            } else {
                                firstSet[getIndex(target)].add(c);//将FIRST(Y)中的非$就加入FIRST(X)
                            }
                        }
                        if (flag == 0) {
                            break;
                        } else {
                            tag += flag;
                            flag = 0;
                        }
                    }
                    if (tag == analyse[i].right.length()) {
                        firstSet[getIndex(target)].add('$');
                    }
                }
            }
        }
    }

    //生成指定非终结符的follow集
    public void getFollow(char target) {
        for (int i = 0; i < num; ++i) {
            int index = -1;
            int len = analyse[i].right.length();
            for (int j = 0; j < len; ++j) {
                if (analyse[i].right.charAt(j) == target) {
                    index = j;
                    break;
                }
            }
            if (index != -1 && index < len - 1) {
                char next = analyse[i].right.charAt(index + 1);
                if (!isNotSymbol(next)) {
                    followSet[getIndex(target)].add(next);
                } else {
                    boolean isExt = false;
                    for (char c : firstSet[getIndex(next)]) {
                        if (c == '$') {
                            isExt = true;
                        } else {
                            followSet[getIndex(target)].add(c);
                        }
                    }

                    if (isExt && analyse[i].left != target) {
                        getFollow(analyse[i].left);
                        for (char c : followSet[getIndex(analyse[i].left)]) {
                            followSet[getIndex(target)].add(c);
                        }
                    }
                }
            } else if (index != -1 && index == len - 1 && target != analyse[i].left) {
                getFollow(analyse[i].left);
                for (char c : followSet[getIndex(analyse[i].left)]) {
                    followSet[getIndex(target)].add(c);
                }
            }
        }
    }

    public void input() {
        initialize();
        Scanner scan = new Scanner(System.in);
        System.out.println("输入产生式的个数");
        num = scan.nextInt();
        scan.nextLine();
        for (int i = 0; i < num; ++i) {
            String str = scan.nextLine();
            System.out.println(str);
            StringBuilder sb = new StringBuilder("");
            for (int j = 0; j < str.length(); ++j) {
                if (str.charAt(j) != ' ') {
                    sb.append(str.charAt(j));
                }
            }
            analyse[i].left = sb.charAt(0);
            analyse[i].right = sb.substring(3);
            System.out.println(analyse[i].right);

            for (int j = 0; j < sb.length(); ++j) {
                if (sb.charAt(j) != '-' && sb.charAt(j) != '>') {
                    if (isNotSymbol(sb.charAt(j))) {
                        boolean flag = false;
                        for (int k = 0; k < nonTerminal.size(); ++k) {
                            if (nonTerminal.get(k) == sb.charAt(j)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            nonTerminal.add(sb.charAt(j));
                        }
                    } else {
                        boolean flag = false;
                        for (int k = 0; k < terminal.size(); ++k) {
                            if (terminal.get(k) == sb.charAt(j)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            terminal.add(sb.charAt(j));
                        }
                    }
                }
            }
        }

        terminal.add('#');
        for (int i = 0; i < nonTerminal.size(); ++i) {
            getFirst(nonTerminal.get(i));
        }

        for (int i = 0; i < nonTerminal.size(); ++i) {
            if (i == 0) {
                followSet[0].add('#');
            }
            getFollow(nonTerminal.get(i));
        }

        for (int i = 0; i < terminal.size(); ++i) {
            if (terminal.get(i) != '$') {
                nonBlankTerminal.add(terminal.get(i));
            }
        }
    }

    public void display() {
        System.out.println("first:");
        for (int i = 0; i < nonTerminal.size(); ++i) {
            System.out.print(nonTerminal.get(i) + ":");
            System.out.println(firstSet[i]);
        }

        System.out.println("follow:");
        for (int i = 0; i <nonTerminal.size(); ++i) {
            System.out.println(nonTerminal.get(i) + ":");
            System.out.println(followSet[i]);
        }
    }

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        grammar.input();
        grammar.display();
    }


    private void initialize() {
        for (int i = 0; i < 100; ++i) {
            analyse[i] = new Node();
            firstSet[i] = new HashSet<Character>();
            followSet[i] = new HashSet<Character>();
        }
    }
}


/**
 * 产生式的结构
 */
class Node {
    char left;
    String right;

    public char getLeft() {
        return left;
    }

    public void setLeft(char left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }
}