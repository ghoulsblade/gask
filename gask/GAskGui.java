package gask;


import gask.GAsk.GAskQuestion;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.gui.GuiTextField;
import cpw.mods.fml.client.FMLClientHandler;

public class GAskGui extends GuiScreen {
	int iQuestionID = 0;
	GAskQuestion mQ;
	GuiTextField answer_freetext;

    public GAskGui(int _iQuestionID,GAskQuestion o)
    {
    	// this.editingPlayer = par1EntityPlayer;
    	iQuestionID = _iQuestionID;
    	mQ = o;
    	GAskUtils.debug("GAskGui created");
    }
    
    public GAskQuestion getQuestion ()
    {
    	return mQ;
    	//GAsk g = GAsk.instance;
    	//return g.questions.get(iCurrentQuestion);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	GAskUtils.debug("GAskGui initGui");
    	initQ();
    }
    


    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        if (answer_freetext != null) this.answer_freetext.textboxKeyTyped(par1, par2);

        if (par2 == 15)
        {
        	if (answer_freetext != null) this.answer_freetext.setFocused(!this.answer_freetext.isFocused());
        }

        /*
        if (par2 == 28 || par2 == 156)
        {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
        */
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);

        if (answer_freetext != null) answer_freetext.mouseClicked(par1, par2, par3);
    }
    
    

	public void initQ()
	{
		GAsk.GAskQuestion o = getQuestion();
        this.buttonList.clear();
        if (o == null) return;

       	int b_width = 200;
       	int b_height = 20;

    	int x = this.width / 4 - b_width/2;
    	int y = this.height / 4 + 40;
    	int ystep = b_height + b_height/10;
    	int x_off = b_width + 20;
    	
    	// = private_get_int(new GuiSmallButton(0, 0, 0, "test"),"height");
    	
    	int anum = o.answers.length;
    	int i;

		// random order
		ArrayList<Integer> shuffled_answer_ids = new ArrayList<Integer>();
		for(i=0;i<anum;++i) shuffled_answer_ids.add(i);
		Collections.shuffle(shuffled_answer_ids);
    	
        for (i=0;i<anum;++i)
        {
        	int reali = shuffled_answer_ids.get(i);
        	String s = o.answers[reali];
        	GAskUtils.debug("GAskGui initQ add answer : "+s);
        	
        	if ((i % 2) == 0)
        	{
	        	GuiSmallButton b = new GuiSmallButton(reali, x        , y, b_width, b_height, s); // left side
	        	this.buttonList.add(b);
        	} else 
        	{
	        	GuiSmallButton b = new GuiSmallButton(reali, x + x_off, y, b_width, b_height, s); // right side
	        	this.buttonList.add(b);
        		y += ystep;
        	}
        }

        // free text answer
        if (anum == 0) 
        {
            // public GuiTextField(FontRenderer par1FontRenderer, int xPos, int yPos, int width, int height)
        	x = this.width / 2 - b_width / 2;
    		answer_freetext = new GuiTextField(this.fontRenderer, x, y, b_width, b_height);
            answer_freetext.setFocused(true);
            answer_freetext.setText("");

    		y += ystep;
    		
        	GuiSmallButton b = new GuiSmallButton(42, x, y, b_width, b_height, "Answer");
        	this.buttonList.add(b);
    		y += ystep;
        	
        	
        }
	}

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton btn)
    {
    	GAskUtils.debug("GAskGui actionPerformed");
        ///this.field_140048_a.confirmClicked(par1GuiButton.id == 0, this.field_140044_d);
    	
    	String sAnswer = "";
    	if (answer_freetext != null) sAnswer = answer_freetext.getText();

    	GAskPacketHandler.SendToServer_Answer(this.iQuestionID,btn.id,sAnswer);
        FMLClientHandler.instance().getClient().displayGuiScreen(null);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();

		GAsk.GAskQuestion o = getQuestion();
        if (o == null) return;

        this.drawCenteredString(this.fontRenderer, o.q, this.width / 2, this.height / 4, 16777215);

        if (answer_freetext != null) answer_freetext.drawTextBox();
        
        /*
        // this.drawCenteredString(this.fontRenderer, "blabla1", this.width / 2, 70, this.field_140045_e.field_140075_c);
        this.drawCenteredString(this.fontRenderer, "blabla2", this.width / 2, 90, 16777215);
        this.drawCenteredString(this.fontRenderer, "blabla3", this.width / 2, 110, 16777215);
        */
        super.drawScreen(par1, par2, par3);
    }
    
    
    /*
    @Override
    protected void drawGuiContainerForegroundLayer(int param1, int param2) {
            //draw text and stuff here
            //the parameters for drawString are: string, x, y, color
            fontRenderer.drawString("Tiny", 8, 6, 4210752);
            //draws "Inventory" or your regional equivalent
            fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2,
                    int par3) {
            //draw your Gui here, only thing you need to change is the path
            int texture = mc.renderEngine.getTexture("/gui/trap.png");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.renderEngine.bindTexture(texture);
            int x = (width - xSize) / 2;
            int y = (height - ySize) / 2;
            this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
    */

}