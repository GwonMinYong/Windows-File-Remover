import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.text.similarity.JaroWinklerDistance;

public class SameFileRemover {
    private JFrame frame;
    private JTextField fldFolder;
    private JTextArea txtFiles;
    private JButton btnBrowse, btnList, btnRemove;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                SameFileRemover window = new SameFileRemover();
                window.frame.setVisible(true);
                window.frame.setSize(600, 600);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SameFileRemover() {
        initComponents();
    }

    private void initComponents() {
        frame = new JFrame("File Remover");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel pnlTop = new JPanel();
        FlowLayout fl_pnlTop = (FlowLayout) pnlTop.getLayout();
        fl_pnlTop.setAlignment(FlowLayout.LEFT);
        frame.getContentPane().add(pnlTop, BorderLayout.NORTH);

        pnlTop.add(new JLabel("폴더 경로:"));

        fldFolder = new JTextField();
        pnlTop.add(fldFolder);
        fldFolder.setColumns(40);

        btnBrowse = new JButton("찾아보기");
        btnBrowse.addActionListener(this::btnBrowseClicked);
        pnlTop.add(btnBrowse);

        JPanel pnlCenter = new JPanel();
        frame.getContentPane().add(pnlCenter, BorderLayout.CENTER);
        pnlCenter.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        pnlCenter.add(scrollPane);

        txtFiles = new JTextArea();
        txtFiles.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        txtFiles.setEditable(false);
        scrollPane.setViewportView(txtFiles);

        JPanel pnlBottom = new JPanel();
        frame.getContentPane().add(pnlBottom, BorderLayout.SOUTH);

        btnList = new JButton("파일 목록 가져오기");
        btnList.addActionListener(this::btnListClicked);
        pnlBottom.add(btnList);

        btnRemove = new JButton("선택된 파일 삭제");
        btnRemove.addActionListener(this::btnRemoveClicked);
        pnlBottom.add(btnRemove);

        frame.pack();
    }

    // "찾아보기" 버튼 클릭시 이벤트
    private void btnBrowseClicked(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showOpenDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            fldFolder.setText(folder.getPath());
        }
    }

    // "파일 목록 가져오기" 버튼 클릭시 이벤트
    private void btnListClicked(ActionEvent event) {
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        String folderPath = fldFolder.getText();
        File folder = new File(folderPath);
        txtFiles.setText("");

        if (folder.exists() && folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            Arrays.sort(fileList, Comparator.comparing(File::length));
            long frontLen=0;
            long nowLen=0;
            String frontName="<:>";
            String frontExtension="<:>";
            String nowExtension="";
            String nowName="";

            double similarity = 0;
            for (File file : fileList) {


                nowLen=file.length();
                nowExtension=getExtension(file.getName());
                nowName=cutExtension(file.getName());
                similarity = jaroWinklerDistance.apply(frontName,nowName);
                System.out.println(frontName);
                System.out.println(nowName);
                System.out.println((nowName.matches(".*\\(\\d\\)$") || file.getName().contains(frontName)));
                System.out.println((frontLen==nowLen && nowExtension.equals(frontExtension)));
                System.out.println();
                if(!file.isFile())
                {
                    continue;
                }


                if ((nowName.endsWith("복사본")||(similarity>0.8)||nowName.matches(".*\\(\\d\\)$") || file.getName().contains(frontName)) &&
                        (frontLen==nowLen && nowExtension.equals(frontExtension))) {
                    txtFiles.insert(file.getName() + "\n",0);
                }
                frontName=nowName;
                frontExtension=nowExtension;
                frontLen=nowLen;
            }


        } else {
            txtFiles.setText("폴더를 찾을 수 없거나, 정확한 경로를 입력해주세요.");
        }
    }

    public static boolean isEqualFile(File front, File now)
    {
        if(front==null || now == null)
            return false;
        return (now.getName().matches(".*\\(\\d\\)$") || now.getName().contains(cutExtension(front.getName()))) &&
                (front.length()==now.length() && getExtension(now.getName()).equals(getExtension(front.getName())));
    }
    public static String cutExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) {
            // "." 이 없는 경우
            return fileName;
        } else if (lastIndex == 0) {
            // "." 이 파일명 맨 앞에 있는 경우
            return "";
        } else {
            // "." 이 파일명 중간에 있는 경우
            return fileName.substring(0, lastIndex);
        }
    }
    public static String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) {
            // "." 이 없는 경우
            return "";
        } else if (lastIndex == 0) {
            // 유일한 "." 이 파일명 맨 앞에 있는 경우
            return "";
        } else {
            // "." 이 파일명 중간에 있는 경우
            return fileName.substring(lastIndex, fileName.length());
        }
    }
    // "선택된 파일 삭제" 버튼 클릭시 이벤트
    private void btnRemoveClicked(ActionEvent event) {
        String folderPath = fldFolder.getText();
        File folder = new File(folderPath);
        String[] fileNames = txtFiles.getText().split("\n");

        for (String fileName : fileNames) {
            File file = new File(folder, fileName);

            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }

        btnListClicked(event);
    }

}

