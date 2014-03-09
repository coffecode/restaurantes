package codecoffe.restaurantes.utilitarios;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;

public class FocusTraversal extends FocusTraversalPolicy
{
	ArrayList<Component> ordem;

	public FocusTraversal(ArrayList<Component> order) {
		ordem = new ArrayList<Component>();
		this.ordem.addAll(order);
	}
	
	@Override
	public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {		
		int index = ordem.indexOf(aComponent);
		if((index + 1) >= ordem.size()) {
			return ordem.get(0);
		}
		return ordem.get(index + 1);
	}
	
	@Override
	public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
		int idx = ordem.indexOf(aComponent) - 1;
		if (idx < 0) {
			idx = ordem.size() - 1;
		}
		return ordem.get(idx);
	}
	
	@Override
	public Component getDefaultComponent(Container focusCycleRoot) {
		return ordem.get(0);
	}
	
	@Override
	public Component getLastComponent(Container focusCycleRoot) {
		return ordem.get((ordem.size()-1));
	}
	
	@Override
	public Component getFirstComponent(Container focusCycleRoot) {
		return ordem.get(0);
	}
}