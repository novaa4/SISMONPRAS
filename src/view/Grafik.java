package view;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import koneksi.Koneksi;

public class Grafik extends JPanel {
    public Grafik() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); 
        refreshGrafik();
    }

    public void refreshGrafik() {
        removeAll(); 
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        //  QUERY UNION AGAR SINKRON DENGAN DASHBOARD 
        String sql = "SELECT nama_tingkat, SUM(jumlah) AS total FROM (" +
                     "  SELECT t.nama_tingkat, COUNT(*) AS jumlah " +
                     "  FROM prestasi_akademik p " +
                     "  JOIN tingkat t ON p.id_tingkat = t.id_tingkat " +
                     "  GROUP BY t.nama_tingkat " +
                     "  UNION ALL " +
                     "  SELECT t.nama_tingkat, COUNT(*) AS jumlah " +
                     "  FROM prestasi_non p " + 
                     "  JOIN tingkat t ON p.id_tingkat = t.id_tingkat " +
                     "  GROUP BY t.nama_tingkat" +
                     ") AS gabungan GROUP BY nama_tingkat";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                dataset.addValue(rs.getInt("total"), "Prestasi", rs.getString("nama_tingkat"));
            }
        } catch (Exception e) { // MODUL 7: Exception Handling
            e.printStackTrace(); 
        }

        // Chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Statistik Tingkat Prestasi (Gabungan)", 
                "Tingkat", 
                "Jumlah", 
                dataset,
                PlotOrientation.VERTICAL,
                false, 
                true, 
                false
        );

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setOutlineVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Color[] pastelColors = {
                    new Color(255, 179, 186), 
                    new Color(186, 225, 255), 
                    new Color(186, 255, 201)  
                };
                return pastelColors[column % pastelColors.length];
            }
        };

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.2); 
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(chartPanel, BorderLayout.CENTER);
        
        revalidate(); 
        repaint();
    }
}