package burlton.dartzee.code.screen;

import burlton.core.code.util.Debug;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.screen.game.DartsScorerRoundTheClock;
import burlton.dartzee.code.utils.DartsColour;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class TestScreen extends EmbeddedScreen
{
	public TestScreen() {
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][grow][][][grow]", "[grow][][][]"));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, "cell 0 0");
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_1.add(table, BorderLayout.CENTER);
		
		table.setPreferredScrollableViewportSize(new Dimension(50, 100));
		
		
		btnTest.addActionListener(this);
		btnTest_1.addActionListener(this);
		
		panel.add(panel_3, "cell 1 0,grow");
		panel_3.setLayout(null);
		
		
		dartboard.setBounds(12, 13, 60, 60);
		panel_3.add(dartboard);
		
		panel.add(panel_2, "cell 4 0,grow");
		panel.add(btnTest, "cell 0 2");
		
		panel.add(btnTest_1, "cell 0 3");
		
		panel_2.init(new PlayerEntity(), "Any");
	}
	
	private final ScrollTableDartsGame table = new ScrollTableDartsGame();
	private final JButton btnTest = new JButton("Translate Points");

	private final JButton btnTest_1 = new JButton("Test");
	private final DartsScorerRoundTheClock panel_2 = new DartsScorerRoundTheClock();
	private final JPanel panel_3 = new JPanel();
	private final Dartboard dartboard = new Dartboard(16, 16);
	
	@Override
	public String getScreenName()
	{
		return "*** TESTING ***";
	}

	@Override
	public void initialise()
	{
		ColourWrapper wireframe = new ColourWrapper(DartsColour.TRANSPARENT);
		wireframe.setEdgeColour(Color.BLACK);
		dartboard.paintDartboard(wireframe, false);
		
		DefaultModel model = new DefaultModel();
		model.addColumn("Game");
		
		for (int i=0; i<5; i++)
		{
			Object[] row = {i};
			
			model.addRow(row);
		}
		
		table.setModel(model);
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnTest)
		{
			List<String> colorKeys = new ArrayList<>();
		    Set<Entry<Object, Object>> entries = UIManager.getLookAndFeelDefaults().entrySet();
		    for (Entry entry : entries)
		    {
			      if (entry.getValue() instanceof Color)
			      {
			    	  Debug.append(entry.getKey() + " | " + entry.getValue());
			      }
		    }

		    // sort the color keys
		    Collections.sort(colorKeys);
		    
		}
		else if (arg0.getSource() == btnTest_1)
		{
			Dart drt = new Dart(20, 1);
			drt.setStartingScore(20);
			panel_2.addDart(null);
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
}