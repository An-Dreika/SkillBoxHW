import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class Main {
    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final String SRC_FOLDER = "/users/andrew/Desktop/src";
    private static final String DST_FOLDER = "/users/andrew/Desktop/dst";

    public static void main(String[] args) throws InterruptedException {
        resizeImages(300, 1); // work with 1 thread
        resizeImages(300, NUM_PROCESSORS); // work with a number of available processors
    }

    private static void resizeImages(int newWidth, int threadsCount) throws InterruptedException {
        threadsCount = Math.min(threadsCount, NUM_PROCESSORS);

        System.out.println(String.format("%nThreads count: %d", threadsCount)
                + "\nConversion start. Please wait...");

        File[] filesInFolder = new File(SRC_FOLDER).listFiles();

        if (filesInFolder != null) {
            Arrays.sort(filesInFolder, Comparator.comparing(File::length)); // sorting files by length
            List<List<File>> files = splitFiles(filesInFolder, threadsCount);

            long start = System.currentTimeMillis();

            transFilesInThreads(files, newWidth);

            System.out.println(String.format("Duration: %d ms", (System.currentTimeMillis() - start))
                    + "\nConversion finish\n");
        } else {
            System.out.println(String.format("Directory %s is empty or not exist", SRC_FOLDER));
        }
    }

    // split files for each thread
    private static List<List<File>> splitFiles(File[] files, int partsCount) {
        List<List<File>> lists = new ArrayList<>();

        for (int i = 0; i < partsCount; i++) {
            lists.add(new ArrayList<>());
        }

        int partNumber = 0;
        for (File file : files) {
            lists.get(partNumber).add(file);
            partNumber++;
            if (partNumber == partsCount)
                partNumber = 0;
        }
        return lists;
    }

    private static void transFilesInThreads(List<List<File>> files, int newWidth) throws InterruptedException {
        List<Thread> threads = new LinkedList<>();
        for(List<File> fileList : files) {

            threads.add(new Thread(() -> {
                for (File file : fileList) {
                    try {
                        convertImage(file, newWidth);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // process of resizing files
    private static void convertImage(File file, int newWidth) throws IOException {
        BufferedImage image = ImageIO.read(file);

        if (image != null) {
            long start = System.currentTimeMillis();

            BufferedImage newImage =
                    Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, newWidth, Scalr.OP_ANTIALIAS);

            File newFile = new File(DST_FOLDER + "/" + formatFile(file));
            ImageIO.write(newImage, "jpg", newFile);

            long duration = System.currentTimeMillis() - start;
            processInfo(file, image, newFile, newImage, duration);
        }
    }

    // make shorter the names of images
    static String formatFile(File file) {
        String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        return (fileName.length() <= 8 ? fileName : fileName.substring(0, 8) + "~") + ".jpg";
    }

    // print the process of resizing
    private static void processInfo(File file, BufferedImage image, File newFile, BufferedImage newImage, long duration) {

        long threadId = Thread.currentThread().getId();
        String name = Main.formatFile(file);
        long size = file.length() / 1024;
        int width = image.getWidth();
        int height = image.getHeight();

        String newName = Main.formatFile(newFile);
        long newSize = newFile.length() / 1024;
        int newWidth = newImage.getWidth();
        int newHeight = newImage.getHeight();

        String message = String.format(
                "Thread id: %s\t%-13s\t%6s kb\t%s x %s px\t---> \t%-12s\t%6s kb\t%s x %s px\t %-6d ms",
                threadId, name, size, width, height, newName, newSize, newWidth, newHeight, duration);
        System.out.println(message);
    }
}