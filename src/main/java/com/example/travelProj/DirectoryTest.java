package com.example.travelProj;

import java.io.File;
import java.io.IOException;

public class DirectoryTest {
    public static void main(String[] args) {
        File dir = new File("/Users/mirikwon/IdeaProjects/travel/uploads/profile/1/test_file.txt");
        try {
            if (dir.createNewFile()) {
                System.out.println("파일 생성 성공: " + dir.getAbsolutePath());
            } else {
                System.out.println("파일이 이미 존재합니다.");
            }
        } catch (IOException e) {
            System.err.println("파일 생성 중 오류 발생: " + e.getMessage());
        }
    }
}