///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package vn.mobileid.exsig;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * @author Admin
// */
//public class testCheckPermission {
//    public static void main(String[] args) throws IOException, Exception{
////        String path = "C:\\Users\\Admin\\Desktop\\file\\CheckCertified_Signed\\Certified_FormFillingAndAnnotation.pdf";
////String path = "C:\\Users\\Admin\\Desktop\\file\\CheckCertified_Signed\\Certified_NoChangeAllowed.pdf";
//        String path = "C:\\Users\\Admin\\Desktop\\file\\CheckCertified_Signed\\NoCertified.pdf";
//        PdfProfileCMS pdfCMS = new PdfProfileCMS(PdfForm.B, Algorithm.SHA256);
//        byte[] src = Files.readAllBytes(new File(path).toPath());
//        List<byte[]> list = new ArrayList<>();
//        list.add(src);
//        System.out.println("Can append new Signature?:"+pdfCMS.checkPermission(list));
//        
//    }
//}
