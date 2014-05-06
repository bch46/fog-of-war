package fow.app;

public class MainScreen extends AbstractScreen {
	
	protected final MainView view;
	
	private ServerConnection sc;
	
	public MainScreen(Main game) {
		super(game);
		
		view = new MainView(this);
		stage.addActor(view);
	}
	
	public void onConnectPressed() {
		String ipAddress = view.getIPText().trim();
		if (ipAddress.equals("")) {
			System.out.println("Can't connect due to empty IP");
			return;
		}

		String idText = view.getIdText().trim();
		if (idText.equals("")) {
			System.out.println("Can't connect due to no ID");
			return;
		}
		
		if (Utils.validateIP(ipAddress)) {
			int id = Integer.valueOf(idText).intValue();
			sc = new ServerConnection(ipAddress, Main.port, id, true);
			sc.connect();
		} else {
			System.out.println("Can't connect due to invalid IP");
		}
	}

}
