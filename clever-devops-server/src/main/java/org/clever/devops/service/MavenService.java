//package org.clever.devops.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.maven.cli.MavenCli;
//import org.clever.common.server.service.BaseService;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//
///**
// * Maven服务<br/>
// */
//@Service
//@Slf4j
//public class MavenService extends BaseService {
//
//    private static final String settings = "D:\\ToolsSoftware\\Maven\\settings.xml";
//
//    // TODO Maven编译项目
//    @SuppressWarnings("deprecation")
//    public void test03(String directory) throws IOException {
//        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, System.getProperty("java.home"));
//
//        MavenCli maven = new MavenCli();
//        String[] args =
//                new String[]{
//                        "clean",
//                        "install",
//                        "-Dmaven.test.skip=true",
////                        "-Pdev",
//                        "-U",
//                        "--global-settings=" + settings,
////                        ""
//                };
//        maven.doMain(args, directory, System.out, System.err);
//    }
//}
//package org.clever.devops.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.maven.cli.MavenCli;
//import org.clever.common.server.service.BaseService;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//
///**
// * Maven服务<br/>
// */
//@Service
//@Slf4j
//public class MavenService extends BaseService {
//
//    private static final String settings = "D:\\ToolsSoftware\\Maven\\settings.xml";
//
//    // TODO Maven编译项目
//    @SuppressWarnings("deprecation")
//    public void test03(String directory) throws IOException {
//        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, System.getProperty("java.home"));
//
//        MavenCli maven = new MavenCli();
//        String[] args =
//                new String[]{
//                        "clean",
//                        "install",
//                        "-Dmaven.test.skip=true",
////                        "-Pdev",
//                        "-U",
//                        "--global-settings=" + settings,
////                        ""
//                };
//        maven.doMain(args, directory, System.out, System.err);
//    }
//}
