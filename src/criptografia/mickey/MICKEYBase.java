package criptografia.mickey;

import java.util.Arrays;

/**
 * Some general things all MICKEYs do share.
 */
public abstract class MICKEYBase implements ICipher {
    protected int R0, R1, R2;
    protected int S0, S1, S2;
    protected int[] key;
    protected int nsize;
    
    protected int[] cached_setupNonce_R;
    protected int[] cached_setupNonce_S;
    
    ///////////////////////////////////////////////////////////////////////////
    
    public void setNonceSize(int nsize) {
        this.nsize = nsize;     
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public MICKEYBase() {
        this.nsize = getNonceSize();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getNonceSize() {
        return getKeySize();      
    }
    
    public int getWordSize() {
        return 1;
    }
    
    public boolean isPatented() {
        return false;
    }
    
    public void reset() throws Exception {
    }

    public void erase() {
        Arrays.fill(this.key, 0);
    }
}
