package com.nexiwave.tools.eclipse.highlightonselection;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.nexiwave.tools.eclipse.highlightonselection.preferences.PreferenceConstants;

public class TextSelectionListener implements ISelectionListener {
	
//	private Color fColor = null;
	private Color bColor = null;
	
	private HashSet<ITextViewer> viewersWithStyleChange = new HashSet<ITextViewer>();
	
	public TextSelectionListener() {
		super();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (PreferenceConstants.P_BACKGROUND_COLOR.equals(event.getProperty())) {
					bColor = null;
				}
			}
		});
	}

	public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
		log("Selection changed");
		if (!(sourcepart instanceof IEditorPart)) {
			log("Caller is not an editor. Caller=|" + sourcepart + "|");
			return;
		}
		
		IEditorPart editor = (IEditorPart)sourcepart;
		ITextViewer viewer = (ITextViewer) sourcepart.getAdapter(ITextOperationTarget.class);
		if (viewer == null) {
			log("No viewer found from caller. Caller=|" + sourcepart + "|");
			return;
		}
		
		if (selection instanceof ITextSelection) {
			
			ITextSelection ts = (ITextSelection) selection;
			String s = ts.getText();
			
			if (s == null || s.trim().length() == 0) {
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				if (!store.getBoolean(PreferenceConstants.P_KEEP_HIGHLIGHTS)) {
					clear(viewer);
				}
				else {
					log("Keeping highlights");
				}
			}
			else {
				highlight(editor, viewer, s.trim());
			}
			
		}
	}
	
	private void clear(ITextViewer viewer) {
		StyledText text = viewer.getTextWidget();
		if ((text == null) || text.isDisposed()) { return; }
		
		if (viewersWithStyleChange.contains(viewer)) {
			log("Clear view for : |" + viewer + "|");

			viewer.invalidateTextPresentation();
			viewersWithStyleChange.remove(viewer);
		}
	}
	
	private void highlight(IEditorPart editor, ITextViewer viewer, String s) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (!store.getBoolean(PreferenceConstants.P_ENABLE)) {
			log("Highlight is not enabled");
			return;
		}
		IFileEditorInput ife = (IFileEditorInput)editor.getEditorInput().getAdapter(IFileEditorInput.class);
		String ext = ife.getFile().getFileExtension();
		
		String config = PreferenceConstants.getConfigForExt(ext);
		String val = store.getString(config);
		if (val == null || val.length() == 0) {
			config = PreferenceConstants.getConfigForExt("*");
		}
		if (!store.getBoolean(config)) {
			log("Highlight is not enabled for ext " + config);
			return;
		}
		
		StyledText text = viewer.getTextWidget();
		if ((text == null) || text.isDisposed()) { return; }
		
		clear(viewer);
		
		if (s.contains("\n"))
			return;
		
		log("Highlighting these text: |" + s + "|");
		
		s = "(" + Pattern.quote(s) + ")";
		if (store.getBoolean(PreferenceConstants.P_WORDS_ONLY)) {
			s = "\\b" + s + "\\b";
		}
		if (!store.getBoolean(PreferenceConstants.P_CASE_SENSITIVE)) {
			s = "(?i)" + s;
		}
		
		Pattern p = Pattern.compile(s);
		
		String stext = text.getText();
		
		Matcher m = p.matcher(stext);
		while (m.find()) {
			IRegion region = new Region(m.start(1), m.end(1) - m.start(1));
			
			// highlight region
//		    int offset = -1;
//		    int length = 0;
//		    
//			if (viewer instanceof ITextViewerExtension5) {
//				ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
//		        IRegion widgetRange = extension.modelRange2WidgetRange(region);
//		        if (widgetRange != null) { 
//			        offset = widgetRange.getOffset();
//			        length = widgetRange.getLength();
//		        }
//		        else
//		        	continue;
//			} 
//			if (offset < 0) {
//		        offset = region.getOffset() - viewer.getVisibleRegion().getOffset();
//		        length = region.getLength();
//		    }
			int offset = region.getOffset();
	        int length = region.getLength();
			
			TreeSet<StyleRange> newStyleRanges = new TreeSet<StyleRange>(new Comparator<StyleRange>() {

				@Override
				public int compare(StyleRange o1, StyleRange o2) {
					if (o1.start < o2.start)
						return -1;
					return 1;
				}
			});
			
		    StyleRange[] oldStyleRanges = text.getStyleRanges(offset, length);
		    
		    for (int i=0; i<oldStyleRanges.length; i++) {
		    	StyleRange oldStyleRange = oldStyleRanges[i];	    
			    Color foregroundColor = getForegroundColor(text, oldStyleRange);
			    Color backgroundColor = getBackgroundColor(text, oldStyleRange);
			    
			    newStyleRanges.add(new StyleRange(oldStyleRange.start, oldStyleRange.length, foregroundColor, backgroundColor));
		    }
		    
	    	int end = offset + length;
		    NEXT_CHAR:
		    for (int i=offset; i<end; i++) {
		    	for (StyleRange sr : newStyleRanges) {
		    		if (i < sr.start) {
		    			newStyleRanges.add(new StyleRange(i, sr.start, getForegroundColor(text, null), getBackgroundColor(text, null)));
		    			
		    			i = sr.start + sr.length - 1;
		    			continue NEXT_CHAR;
		    		}
		    		if (sr.start <= i && i < sr.start + sr.length) {
		    			// in range:
		    			i = sr.start + sr.length - 1;
		    			continue NEXT_CHAR;
		    		}
		    	}
		    	
		    	//
    			newStyleRanges.add(new StyleRange(i, end - i, getForegroundColor(text, null), getBackgroundColor(text, null)));
    			break;
		    }

		    text.replaceStyleRanges(offset, length, newStyleRanges.toArray(new StyleRange[newStyleRanges.size()]));
		    
		    for (StyleRange sr : newStyleRanges)
		    	text.redrawRange(sr.start, sr.length, true);
		    
		    viewersWithStyleChange.add(viewer);
		}
	}
	
	private Color getForegroundColor(StyledText text, StyleRange oldStyleRange) {
		return (oldStyleRange == null) ? text.getForeground() : oldStyleRange.foreground;
//		if (fColor == null)
//			fColor = new Color(Display.getCurrent(), 0, 0, 255);
//		return fColor;
	}

	private Color getBackgroundColor(StyledText text, StyleRange oldStyleRange) {
		if (bColor == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			
			bColor = new Color(Display.getCurrent(), PreferenceConverter.getColor(store, PreferenceConstants.P_BACKGROUND_COLOR));
		}
		return bColor;
	}
	
	private static void log(String msg) {
		Activator.log(msg);
	}
}