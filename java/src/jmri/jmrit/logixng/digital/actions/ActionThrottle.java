package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleAnalogExpressionSocket;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleAnalogExpressionSocket;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs an engine.
 * This action reads an analog expression with the loco address and sets its
 * speed according to an alaog expression and the direction according to a
 * digital expression.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionThrottle extends AbstractDigitalAction
        implements FemaleSocketListener {

    public static final int LOCO_ADDRESS_SOCKET = 0;
    public static final int LOCO_SPEED_SOCKET = 1;
    public static final int LOCO_DIRECTION_SOCKET = 2;
    
    // The throttle if we have anyone or if a request is sent, null otherwise
    private DccThrottle _throttle;
    private ThrottleListener _throttleListener;
    
    private String _locoAddressSocketSystemName;
    private String _locoSpeedSocketSystemName;
    private String _locoDirectionSocketSystemName;
    private final FemaleAnalogExpressionSocket _locoAddressSocket;
    private final FemaleAnalogExpressionSocket _locoSpeedSocket;
    private final FemaleDigitalExpressionSocket _locoDirectionSocket;
    boolean _isActive = false;
    
    private boolean _listenersAreRegistered = false;
    
    public ActionThrottle(String sys, String user) {
        super(sys, user);
        _locoAddressSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, "E1");
        _locoSpeedSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, "E2");
        _locoDirectionSocket = InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(this, this, "E3");
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        
        int currentLocoAddress = -1;
        int newLocoAddress = -1;
        
        if (_throttle != null) {
            currentLocoAddress = _throttle.getLocoAddress().getNumber();
        }
        
        if (_locoAddressSocket.isConnected()) {
            newLocoAddress =
                    (int) ((MaleAnalogExpressionSocket)_locoAddressSocket.getConnectedSocket())
                            .evaluate();
        }
        
        if (newLocoAddress != currentLocoAddress) {
            
            if (_throttle != null) {
                // Stop the loco
                _throttle.setSpeedSetting(0);
                // Release the loco
                InstanceManager.getDefault(ThrottleManager.class).releaseThrottle(_throttle, _throttleListener);
                _throttle = null;
            }
            
            if (newLocoAddress != -1) {
                
                _throttleListener =  new ThrottleListener() {
                    @Override
                    @Deprecated
                    public void notifyStealThrottleRequired(LocoAddress address) {
//                        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
                        log.warn("Loco {} cannot be aquired. Decision required.", address.getNumber());
                    }

                    @Override
                    public void notifyThrottleFound(DccThrottle t) {
                        _throttle = t;
                        executeConditionalNG();
                    }

                    @Override
                    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                        log.warn("loco {} cannot be aquired", address.getNumber());
                    }

                    @Override
                    public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
                        log.warn("Loco {} cannot be aquired. Decision required.", address.getNumber());
                    }
                };
                
                boolean result = InstanceManager.getDefault(ThrottleManager.class)
                        .requestThrottle(newLocoAddress, _throttleListener);
                
                if (!result) {
                    log.warn("loco {} cannot be aquired", newLocoAddress);
                }
            }
            
        }
        
        // We have a throttle if _throttle is not null
        if (_throttle != null) {
            
            double speed = 0;
            boolean isForward = true;
            
            if (_locoSpeedSocket.isConnected()) {
                speed =
                        ((MaleAnalogExpressionSocket)_locoSpeedSocket.getConnectedSocket())
                                .evaluate();
            }
            
            if (_locoDirectionSocket.isConnected()) {
                isForward =
                        ((MaleDigitalExpressionSocket)_locoDirectionSocket.getConnectedSocket())
                                .evaluate();
            }

            _throttle.setSpeedSetting((float) speed);
            _throttle.setIsForward(isForward);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case LOCO_ADDRESS_SOCKET:
                return _locoAddressSocket;
                
            case LOCO_SPEED_SOCKET:
                return _locoSpeedSocket;
                
            case LOCO_DIRECTION_SOCKET:
                return _locoDirectionSocket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 3;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _locoAddressSocket) {
            _locoAddressSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoSpeedSocket) {
            _locoSpeedSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else if (socket == _locoDirectionSocket) {
            _locoDirectionSocketSystemName = socket.getConnectedSocket().getSystemName();
            executeConditionalNG();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _locoAddressSocket) {
            if (_throttle != null) {
                // Stop the loco
                _throttle.setSpeedSetting(0);
                // Release the loco
                InstanceManager.getDefault(ThrottleManager.class).releaseThrottle(_throttle, _throttleListener);
            }
            _locoAddressSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoSpeedSocket) {
            _locoSpeedSocketSystemName = null;
            executeConditionalNG();
        } else if (socket == _locoDirectionSocket) {
            _locoDirectionSocketSystemName = null;
            executeConditionalNG();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }
    
    private void executeConditionalNG() {
        if (_listenersAreRegistered) {
            ConditionalNG c = getConditionalNG();
            if (c != null) {
                c.execute();
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Throttle_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Throttle_Long", _locoAddressSocket.getName());
    }

    public FemaleAnalogExpressionSocket getLocoAddressSocket() {
        return _locoAddressSocket;
    }

    public String getLocoAddressSocketSystemName() {
        return _locoAddressSocketSystemName;
    }

    public void setLocoAddressSocketSystemName(String systemName) {
        _locoAddressSocketSystemName = systemName;
    }

    public FemaleAnalogExpressionSocket getLocoSpeedSocket() {
        return _locoSpeedSocket;
    }

    public String getLocoSpeedSocketSystemName() {
        return _locoSpeedSocketSystemName;
    }

    public void setLocoSpeedSocketSystemName(String systemName) {
        _locoSpeedSocketSystemName = systemName;
    }

    public FemaleDigitalExpressionSocket getLocoDirectionSocket() {
        return _locoDirectionSocket;
    }

    public String getLocoDirectionSocketSystemName() {
        return _locoDirectionSocketSystemName;
    }

    public void setLocoDirectionSocketSystemName(String systemName) {
        _locoDirectionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_locoAddressSocket.isConnected()
                    || !_locoAddressSocket.getConnectedSocket().getSystemName()
                            .equals(_locoAddressSocketSystemName)) {
                
                String socketSystemName = _locoAddressSocketSystemName;
                _locoAddressSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    _locoAddressSocket.disconnect();
                    if (maleSocket != null) {
                        _locoAddressSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression " + socketSystemName);
                    }
                }
            } else {
                _locoAddressSocket.getConnectedSocket().setup();
            }
            
            if ( !_locoSpeedSocket.isConnected()
                    || !_locoSpeedSocket.getConnectedSocket().getSystemName()
                            .equals(_locoSpeedSocketSystemName)) {
                
                String socketSystemName = _locoSpeedSocketSystemName;
                _locoSpeedSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    _locoSpeedSocket.disconnect();
                    if (maleSocket != null) {
                        _locoSpeedSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression " + socketSystemName);
                    }
                }
            } else {
                _locoSpeedSocket.getConnectedSocket().setup();
            }
            
            if ( !_locoDirectionSocket.isConnected()
                    || !_locoDirectionSocket.getConnectedSocket().getSystemName()
                            .equals(_locoDirectionSocketSystemName)) {
                
                String socketSystemName = _locoDirectionSocketSystemName;
                _locoDirectionSocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .getBeanBySystemName(socketSystemName);
                    _locoDirectionSocket.disconnect();
                    if (maleSocket != null) {
                        _locoDirectionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital expression " + socketSystemName);
                    }
                }
            } else {
                _locoDirectionSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _listenersAreRegistered = true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_throttle != null) {
            InstanceManager.getDefault(ThrottleManager.class)
                    .releaseThrottle(_throttle, _throttleListener);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ActionThrottle.class);

}
