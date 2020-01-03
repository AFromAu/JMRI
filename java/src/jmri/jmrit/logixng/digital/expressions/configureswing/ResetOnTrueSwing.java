package jmri.jmrit.logixng.digital.expressions.configureswing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.ResetOnTrue;

/**
 * Configures an ResetOnTrue object with a Swing JPanel.
 */
public class ResetOnTrueSwing extends AbstractExpressionSwing {

    @Override
    protected void createPanel(Base object) {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName)
            throws BadUserNameException, BadSystemNameException {
        ResetOnTrue expression = new ResetOnTrue(systemName, userName);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ResetOnTrue_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
