/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "<WebSig>" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Institute for
 * Data Communications Systems, <http://www.nue.et-inf.uni-siegen.de/>.
 * The development of this software was partly funded by the European
 * Commission in the <WebSig> project in the ISIS Programme.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xml.security.encryption;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.lang.Integer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.keys.keyresolver.implementations.EncryptedKeyResolver;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.utils.URI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
// import sun.misc.BASE64Encoder;
import org.apache.xml.security.utils.Base64;


/**
 * <code>XMLCipher</code> encrypts and decrypts the contents of
 * <code>Document</code>s, <code>Element</code>s and <code>Element</code>
 * contents. It was designed to resemble <code>javax.crypto.Cipher</code> in
 * order to facilitate understanding of its functioning.
 *
 * @author Axl Mattheus (Sun Microsystems)
 * @author Christian Geuer-Pollmann
 */
public class XMLCipher {

    private static org.apache.commons.logging.Log logger = 
        org.apache.commons.logging.LogFactory.getLog(XMLCipher.class.getName());

	//J-
	/** Triple DES EDE (192 bit key) in CBC mode */
    public static final String TRIPLEDES =                   
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES;
    public static final String AES_128 =                     
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
    public static final String AES_256 =                     
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256;
    public static final String AES_192 =                     
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192;
    public static final String RSA_v1dot5 =                  
        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
    public static final String RSA_OAEP =                    
        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;
    public static final String DIFFIE_HELLMAN =              
        EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH;
    public static final String TRIPLEDES_KeyWrap =           
        EncryptionConstants.ALGO_ID_KEYWRAP_TRIPLEDES;
    public static final String AES_128_KeyWrap =             
        EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
    public static final String AES_256_KeyWrap =             
        EncryptionConstants.ALGO_ID_KEYWRAP_AES256;
    public static final String AES_192_KeyWrap =             
        EncryptionConstants.ALGO_ID_KEYWRAP_AES192;
    public static final String SHA1 =                        
        Constants.ALGO_ID_DIGEST_SHA1;
    public static final String SHA256 =                      
        MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
    public static final String SHA512 =                      
        MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512;
    public static final String RIPEMD_160 =                  
        MessageDigestAlgorithm.ALGO_ID_DIGEST_RIPEMD160;
    public static final String XML_DSIG =                    
        Constants.SignatureSpecNS;
    public static final String N14C_XML =                    
        Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String N14C_XML_WITH_COMMENTS =      
        Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String EXCL_XML_N14C =               
        Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String EXCL_XML_N14C_WITH_COMMENTS = 
        Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
    public static final String BASE64_ENCODING =             
        org.apache.xml.security.transforms.Transforms.TRANSFORM_BASE64_DECODE;
	//J+

    public static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;
    public static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;
    public static final int UNWRAP_MODE  = Cipher.UNWRAP_MODE;
    public static final int WRAP_MODE    = Cipher.WRAP_MODE;
	
    private static final String ENC_ALGORITHMS = TRIPLEDES + "\n" +
        AES_128 + "\n" + AES_256 + "\n" + AES_192 + "\n" + RSA_v1dot5 + "\n" +
        RSA_OAEP + "\n" + TRIPLEDES_KeyWrap + "\n" + AES_128_KeyWrap + "\n" +
        AES_256_KeyWrap + "\n" + AES_192_KeyWrap+ "\n";
    private static final String ALGORITHMS = TRIPLEDES + "\n" +
        AES_128 + "\n" + AES_256 + "\n" + AES_192 + "\n" + RSA_v1dot5 + "\n" +
        RSA_OAEP + "\n" + DIFFIE_HELLMAN + "\n" + TRIPLEDES_KeyWrap + "\n" +
        AES_128_KeyWrap + "\n" +  AES_256_KeyWrap + "\n" +
        AES_192_KeyWrap+ "\n" + SHA1 + "\n" + SHA256 + "\n" + SHA512 + "\n" +
        RIPEMD_160 + "\n" + XML_DSIG + "\n" + N14C_XML + "\n" +
        N14C_XML_WITH_COMMENTS + "\n" + EXCL_XML_N14C + "\n" +
        EXCL_XML_N14C_WITH_COMMENTS;

	
	/** Cipher created during initialisation that is used for encryption */
    private Cipher _contextCipher;
	/** Mode that the XMLCipher object is operating in */
    private int _cipherMode = Integer.MIN_VALUE;
	/** URI of algorithm that is being used for cryptographic operation */
    private String _algorithm = null;
	/** Cryptographic provider requested by caller */
	private String _requestedJCEProvider = null;
	/** Used for creation of DOM nodes in WRAP and ENCRYPT modes */
    private Document _contextDocument;
	/** Instance of factory used to create XML Encryption objects */
    private Factory _factory;
	/** Internal serializer class for going to/from UTF-8 */
    private Serializer _serializer;

	/** Local copy of user's key */
	private Key _key;
	/** Local copy of the kek (used to decrypt EncryptedKeys during a
     *  DECRYPT_MODE operation */
	private Key _kek;

	// The EncryptedKey being built (part of a WRAP operation) or read
	// (part of an UNWRAP operation)

	private EncryptedKey _ek;

	// The EncryptedData being built (part of a WRAP operation) or read
	// (part of an UNWRAP operation)

	private EncryptedData _ed;

    /**
     * Creates a new <code>XMLCipher</code>.
     *
     * @since 1.0.
     */
    private XMLCipher() {
        logger.debug("Constructing XMLCipher...");

        _factory = new Factory();
        _serializer = new Serializer();

    }

    /**
     * Checks to ensure that the supplied algorithm is valid.
     *
     * @param algorithm the algorithm to check.
     * @returm true if the algorithm is valid, otherwise false.
     * @since 1.0.
     */
    private static boolean isValidEncryptionAlgorithm(String algorithm) {
        boolean result = (
            algorithm.equals(TRIPLEDES) ||
            algorithm.equals(AES_128) ||
            algorithm.equals(AES_256) ||
            algorithm.equals(AES_192) ||
            algorithm.equals(RSA_v1dot5) ||
            algorithm.equals(RSA_OAEP) ||
            algorithm.equals(TRIPLEDES_KeyWrap) ||
            algorithm.equals(AES_128_KeyWrap) ||
            algorithm.equals(AES_256_KeyWrap) ||
            algorithm.equals(AES_192_KeyWrap)
        );

        return (result);
    }

    /**
     * Returns an <code>XMLCipher</code> that implements the specified
     * transformation and operates on the specified context document.
     * <p>
     * If the default provider package supplies an implementation of the
     * requested transformation, an instance of Cipher containing that
     * implementation is returned. If the transformation is not available in
     * the default provider package, other provider packages are searched.
     * <p>
     * <b>NOTE<sub>1</sub>:</b> The transformation name does not follow the same
     * pattern as that oulined in the Java Cryptography Extension Reference
     * Guide but rather that specified by the XML Encryption Syntax and
     * Processing document. The rational behind this is to make it easier for a
     * novice at writing Java Encryption software to use the library.
     * <p>
     * <b>NOTE<sub>2</sub>:</b> <code>getInstance()</code> does not follow the
     * same pattern regarding exceptional conditions as that used in
     * <code>javax.crypto.Cipher</code>. Instead, it only throws an
     * <code>XMLEncryptionException</code> which wraps an underlying exception.
     * The stack trace from the exception should be self explanitory.
     *
     * @param transformation the name of the transformation, e.g.,
     *   <code>XMLCipher.TRIPLEDES</code> which is shorthand for
     *   &quot;http://www.w3.org/2001/04/xmlenc#tripledes-cbc&quot;
     * @throws <code>XMLEncryptionException</code>.
     * @see javax.crypto.Cipher#getInstance
     */
    public static XMLCipher getInstance(String transformation) throws
            XMLEncryptionException {
        // sanity checks
        logger.debug("Getting XMLCipher...");
        if (null == transformation)
            logger.error("Transformation unexpectedly null...");
        if(!isValidEncryptionAlgorithm(transformation))
            logger.error("Alogorithm unvalid, expected one of " + ENC_ALGORITHMS);

		XMLCipher instance = new XMLCipher();

        instance._algorithm = transformation;
		instance._key = null;
		instance._kek = null;

        try {
			String jceAlgorithm =
				JCEMapper.translateURItoJCEID(transformation).getAlgorithmID();

            instance._contextCipher = Cipher.getInstance(jceAlgorithm);

            logger.debug("cihper.algoritm = " +
                instance._contextCipher.getAlgorithm());
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (NoSuchPaddingException nspe) {
            throw new XMLEncryptionException("empty", nspe);
        }

        return (instance);
    }

    /**
     * Returns an <code>XMLCipher</code> that implements the specified
     * transformation and operates on the specified context document.
     *
     * @param transformation the name of the transformation, e.g.,
     *   <code>XMLCipher.TRIPLEDES</code> which is shorthand for
     *   &quot;http://www.w3.org/2001/04/xmlenc#tripledes-cbc&quot;
     * @param provider the JCE provider that supplies the transformation
     * @throws <code>XMLEncryptionException</code>.
     */
    public static XMLCipher getProviderInstance(String transformation, String provider)
            throws XMLEncryptionException {
        // sanity checks
        logger.debug("Getting XMLCipher...");
        if (null == transformation)
            logger.error("Transformation unexpectedly null...");
        if(null == provider)
            logger.error("Provider unexpectedly null..");
        if("" == provider)
            logger.error("Provider's value unexpectedly not specified...");
        if(!isValidEncryptionAlgorithm(transformation))
            logger.error("Alogorithm unvalid, expected one of " + ENC_ALGORITHMS);

		XMLCipher instance = new XMLCipher();

        instance._algorithm = transformation;
		instance._requestedJCEProvider = provider;
		instance._key = null;
		instance._kek = null;

        try {
			String jceAlgorithm =
				JCEMapper.translateURItoJCEID(transformation).getAlgorithmID();

            instance._contextCipher = Cipher.getInstance(jceAlgorithm, provider);

            logger.debug("cipher._algorithm = " +
                instance._contextCipher.getAlgorithm());
            logger.debug("provider.name = " + provider);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (NoSuchProviderException nspre) {
            throw new XMLEncryptionException("empty", nspre);
        } catch (NoSuchPaddingException nspe) {
            throw new XMLEncryptionException("empty", nspe);
        }

        return (instance);
    }

    /**
     * Returns an <code>XMLCipher</code> that implements no specific
	 * transformation, and can therefore only be used for decrypt or
	 * unwrap operations where the encryption method is defined in the 
	 * <code>EncryptionMethod</code> element.
	 *
     * @throws <code>XMLEncryptionException</code>.
     */

    public static XMLCipher getInstance()
            throws XMLEncryptionException {
        // sanity checks
        logger.debug("Getting XMLCipher for no transformation...");

		XMLCipher instance = new XMLCipher();

        instance._algorithm = null;
		instance._requestedJCEProvider = null;
		instance._key = null;
		instance._kek = null;
		instance._contextCipher = null;

        return (instance);
    }

    /**
     * Returns an <code>XMLCipher</code> that implements no specific
	 * transformation, and can therefore only be used for decrypt or
	 * unwrap operations where the encryption method is defined in the 
	 * <code>EncryptionMethod</code> element.
	 *
	 * Allows the caller to specify a provider that will be used for
	 * cryptographic operations.
     *
     * @param provider the JCE provider that supplies the cryptographic
	 * needs.
     * @throws <code>XMLEncryptionException</code>.
     */

    public static XMLCipher getProviderInstance(String provider)
            throws XMLEncryptionException {
        // sanity checks

        logger.debug("Getting XMLCipher, provider but no transformation");
        if(null == provider)
            logger.error("Provider unexpectedly null..");
        if("" == provider)
            logger.error("Provider's value unexpectedly not specified...");

		XMLCipher instance = new XMLCipher();

        instance._algorithm = null;
		instance._requestedJCEProvider = provider;
		instance._key = null;
		instance._kek = null;
		instance._contextCipher = null;

        return (instance);
    }

    /**
     * Initializes this cipher with a key.
     * <p>
     * The cipher is initialized for one of the following four operations:
     * encryption, decryption, key wrapping or key unwrapping, depending on the
     * value of opmode.
	 *
	 * For WRAP and ENCRYPT modes, this also initialises the internal 
	 * EncryptedKey or EncryptedData (with a CipherValue)
	 * structure that will be used during the ensuing operations.  This
	 * can be obtained (in order to modify KeyInfo elements etc. prior to
	 * finalising the encryption) by calling 
	 * {@link #getEncryptedData} or {@link #getEncryptedKey}.
     *
     * @param opmode the operation mode of this cipher (this is one of the
     *   following: ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE or UNWRAP_MODE)
     * @param key
     * @see javax.crypto.Cipher#init
     */
    public void init(int opmode, Key key) throws XMLEncryptionException {
        // sanity checks
        logger.debug("Initializing XMLCipher...");

		_ek = null;
		_ed = null;

		switch (opmode) {

		case ENCRYPT_MODE :
			logger.debug("opmode = ENCRYPT_MODE");
			_ed = createEncryptedData(CipherData.VALUE_TYPE, "NO VALUE YET");
			break;
		case DECRYPT_MODE :
			logger.debug("opmode = DECRYPT_MODE");
			break;
		case WRAP_MODE :
			logger.debug("opmode = WRAP_MODE");
			_ek = createEncryptedKey(CipherData.VALUE_TYPE, "NO VALUE YET");
			break;
		case UNWRAP_MODE :
			logger.debug("opmode = UNWRAP_MODE");
			break;
		default :
			logger.error("Mode unexpectedly invalid");
			throw new XMLEncryptionException("Invalid mode in init");
		}

        _cipherMode = opmode;
		_key = key;

    }

	/**
	 * Get the EncryptedData being build
	 *
	 * Returns the EncryptedData being built during an ENCRYPT operation.
	 * This can then be used by applications to add KeyInfo elements and
	 * set other parameters.
	 *
	 * @returns The EncryptedData being built
	 */

	public EncryptedData getEncryptedData() {

		// Sanity checks
		logger.debug("Returning EncryptedData");
		return _ed;

	}

	/**
	 * Get the EncryptedData being build
	 *
	 * Returns the EncryptedData being built during an ENCRYPT operation.
	 * This can then be used by applications to add KeyInfo elements and
	 * set other parameters.
	 *
	 * @returns The EncryptedData being built
	 */

	public EncryptedKey getEncryptedKey() {

		// Sanity checks
		logger.debug("Returning EncryptedKey");
		return _ek;
	}

	/**
	 * Set a Key Encryption Key.
	 * <p>
	 * The Key Encryption Key (KEK) is used for encrypting/decrypting
	 * EncryptedKey elements.  By setting this separately, the XMLCipher
	 * class can know whether a key applies to the data part or wrapped key
	 * part of an encrypted object.
	 *
	 * @param kek The key to use for de/encrypting key data
	 */

	public void setKEK(Key kek) {

		_kek = kek;

	}

	/**
	 * Martial an EncryptedData
	 *
	 * Takes an EncryptedData object and returns a DOM Element that
	 * represents the appropriate <code>EncryptedData</code>
	 * <p>
	 * <b>Note:</b> This should only be used in cases where the context
	 * document has been passed in via a call to doFinal.
	 *
	 * @param encryptedData EncryptedData object to martial
	 * @return the DOM <code>Element</code> representing the passed in
	 * object */

	public Element martial(EncryptedData encryptedData) 
		throws XMLEncryptionException {

		return (_factory.toElement (encryptedData));

	}

	/**
	 * Martial an EncryptedKey
	 *
	 * Takes an EncryptedKey object and returns a DOM Element that
	 * represents the appropriate <code>EncryptedKey</code>
	 *
	 * <p>
	 * <b>Note:</b> This should only be used in cases where the context
	 * document has been passed in via a call to doFinal.
	 *
	 * @param encryptedKey EncryptedKey object to martial
	 * @return the DOM <code>Element</code> representing the passed in
	 * object */

	public Element martial(EncryptedKey encryptedKey) 
		throws XMLEncryptionException {

		return (_factory.toElement (encryptedKey));

	}

	/**
	 * Martial an EncryptedData
	 *
	 * Takes an EncryptedData object and returns a DOM Element that
	 * represents the appropriate <code>EncryptedData</code>
	 *
	 * @param context The document that will own the returned nodes
	 * @param encryptedData EncryptedData object to martial
	 * @return the DOM <code>Element</code> representing the passed in
	 * object */

	public Element martial(Document context, EncryptedData encryptedData) 
		throws XMLEncryptionException {

		_contextDocument = context;
		return (_factory.toElement (encryptedData));

	}

	/**
	 * Martial an EncryptedKey
	 *
	 * Takes an EncryptedKey object and returns a DOM Element that
	 * represents the appropriate <code>EncryptedKey</code>
	 *
	 * @param context The document that will own the created nodes
	 * @param encryptedKey EncryptedKey object to martial
	 * @return the DOM <code>Element</code> representing the passed in
	 * object */

	public Element martial(Document context, EncryptedKey encryptedKey) 
		throws XMLEncryptionException {

		_contextDocument = context;
		return (_factory.toElement (encryptedKey));

	}

    /**
     * Encrypts an <code>Element</code> and replaces it with its encrypted
     * counterpart in the context <code>Document</code>, that is, the
     * <code>Document</code> specified when one calls
     * {@link #getInstance(Document, String) getInstance}.
     *
     * @param element the <code>Element</code> to encrypt.
     * @return the context <code>Document</code> with the encrypted
     *   <code>Element</code> having replaced the source <code>Element</code>.
     */

    private Document encryptElement(Element element) throws
            XMLEncryptionException {
        logger.debug("Encrypting element...");
        if(null == element) 
            logger.error("Element unexpectedly null...");
        if(_cipherMode != ENCRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in ENCRYPT_MODE...");

	if (_algorithm == null) {
	    throw new XMLEncryptionException("XMLCipher instance without transformation specified");
	}
	encryptData(_contextDocument, element, false);

        Element encryptedElement = _factory.toElement(_ed);

        Node sourceParent = element.getParentNode();
        sourceParent.replaceChild(encryptedElement, element);

        return (_contextDocument);
    }

    /**
     * Encrypts a <code>NodeList</code> (the contents of an
     * <code>Element</code>) and replaces its parent <code>Element</code>'s
     * content with this the resulting <code>EncryptedType</code> within the
     * context <code>Document</code>, that is, the <code>Document</code>
     * specified when one calls
     * {@link #getInstance(Document, String) getInstance}.
     *
     * @param content the <code>NodeList</code> to encrypt.
     * @return the context <code>Document</code> with the encrypted
     *   <code>NodeList</code> having replaced the content of the source
     *   <code>Element</code>.
     */
    private Document encryptElementContent(Element element) throws
            XMLEncryptionException {
        logger.debug("Encrypting element content...");
        if(null == element) 
            logger.error("Element unexpectedly null...");
        if(_cipherMode != ENCRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in ENCRYPT_MODE...");

	if (_algorithm == null) {
	    throw new XMLEncryptionException("XMLCipher instance without transformation specified");
	}
	encryptData(_contextDocument, element, true);	

        Element encryptedElement = _factory.toElement(_ed);

        removeContent(element);
        element.appendChild(encryptedElement);

        return (_contextDocument);
    }

    /**
     * Process a DOM <code>Document</code> node. The processing depends on the
     * initialization parameters of {@link #init(int, Key) init()}.
     *
     * @param context the context <code>Document</code>.
     * @param source the <code>Document</code> to be encrypted or decrypted.
     * @return the processed <code>Document</code>.
     * @throws XMLEnccryptionException to indicate any exceptional conditions.
     */
    public Document doFinal(Document context, Document source) throws
            XMLEncryptionException {
        logger.debug("Processing source document...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == source)
            logger.error("Source document unexpectedly null...");

        _contextDocument = context;

        Document result = null;

        switch (_cipherMode) {
        case DECRYPT_MODE:
            result = decryptElement(source.getDocumentElement());
            break;
        case ENCRYPT_MODE:
            result = encryptElement(source.getDocumentElement());
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }

        return (result);
    }

    /**
     * Process a DOM <code>Element</code> node. The processing depends on the
     * initialization parameters of {@link #init(int, Key) init()}.
     *
     * @param context the context <code>Document</code>.
     * @param element the <code>Element</code> to be encrypted.
     * @return the processed <code>Document</code>.
     * @throws XMLEnccryptionException to indicate any exceptional conditions.
     */
    public Document doFinal(Document context, Element element) throws
            XMLEncryptionException {
        logger.debug("Processing source element...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == element)
            logger.error("Source element unexpectedly null...");

        _contextDocument = context;

        Document result = null;

        switch (_cipherMode) {
        case DECRYPT_MODE:
            result = decryptElement(element);
            break;
        case ENCRYPT_MODE:
            result = encryptElement(element);
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }

        return (result);
    }

    /**
     * Process the contents of a DOM <code>Element</code> node. The processing
     * depends on the initialization parameters of
     * {@link #init(int, Key) init()}.
     *
     * @param context the context <code>Document</code>.
     * @param element the <code>Element</code> which contents is to be
     *   encrypted.
     * @return the processed <code>Document</code>.
     * @throws XMLEnccryptionException to indicate any exceptional conditions.
     */
    public Document doFinal(Document context, Element element, boolean content)
            throws XMLEncryptionException {
        logger.debug("Processing source element...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == element)
            logger.error("Source element unexpectedly null...");

        _contextDocument = context;

        Document result = null;

        switch (_cipherMode) {
        case DECRYPT_MODE:
            if (content) {
                result = decryptElementContent(element);
            } else {
                result = decryptElement(element);
            }
            break;
        case ENCRYPT_MODE:
            if (content) {
                result = encryptElementContent(element);
            } else {
                result = encryptElement(element);
            }
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }

        return (result);
    }

    /**
     * Process a DOM <code>NodeList</code>. The processing depends on the
     * initialization parameters of {@link #init(int, Key) init()}.
     *
     * @param context the context <code>Document</code>.
     * @param elements the <code>NodeList</code> which contents is to be
     *   processed.
     * @return the processed <code>Document</code>.
     * @throws XMLEnccryptionException to indicate any exceptional conditions.
     */
    private Document doFinal(Document context, NodeList elements) throws
            XMLEncryptionException {
        return (null);
    }

    /**
     * Process an XPath expression. The processing depends on the
     * initialization parameters of {@link #init(int, Key) init()}.
     *
     * @param xpathExpression the expression to process.
     * @return the processed <code>Document</code>.
     * @throws XMLEncryptionException to indicat any exceptional conditions.
     */
    private Document doFinal(String xpathExpression) throws
            XMLEncryptionException {
        return (null);
    }

    /**
     *
     */
    private Document doFinal(Document context, Element element,
            EncryptedData data) throws XMLEncryptionException {
        return (null);
    }

    /**
     *
     */
    private Document doFinal(Document context, Element element,
            EncryptedKey key) throws XMLEncryptionException {
        return (null);
    }

    /**
     * Returns an <code>EncryptedData</code> interface. Use this operation if
     * you want to have full control over the contents of the
     * <code>EncryptedData</code> structure.
	 *
	 * This does not change the source document in any way.
     *
     * @param context the context <code>Document</code>.
     * @param element the <code>Element</code> that will be encrypted.
     * @throws XMLEncryptionException.
     */

    public EncryptedData encryptData(Document context, Element element) throws 
            XMLEncryptionException {
	return encryptData(context, element, false);
    }

    private EncryptedData encryptData(Document context, Element element, boolean contentMode) throws
            XMLEncryptionException {
        logger.debug("Encrypting element...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == element)
            logger.error("Element unexpectedly null...");
        if(_cipherMode != ENCRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in ENCRYPT_MODE...");

        _contextDocument = context;

	if (_algorithm == null) {
	    throw new XMLEncryptionException("XMLCipher instance without transformation specified");
	}
	

        String serializedOctets = null;
	if (contentMode) {
	    NodeList children = element.getChildNodes();
	    if ((null != children)) {
		serializedOctets = _serializer.serialize(children);
	    }
	    else {
		Object exArgs[] = {"Element has no content."};
		throw new XMLEncryptionException("empty", exArgs);
	    }
        }
	else {
	    serializedOctets = _serializer.serialize(element);
	}
	logger.debug("Serialized octets:\n" + serializedOctets);

        byte[] encryptedBytes = null;
		// Now create the working cipher

		String jceAlgorithm =
			JCEMapper.translateURItoJCEID(_algorithm).getAlgorithmID();
		String provider;

		if (_requestedJCEProvider == null)
			provider =
				JCEMapper.translateURItoJCEID(_algorithm).getProviderId();
		else
			provider = _requestedJCEProvider;

		logger.debug("provider = " + provider + "alg = " + jceAlgorithm);

		Cipher c;
		try {
			c = Cipher.getInstance(jceAlgorithm, provider);
		} catch (NoSuchAlgorithmException nsae) {
			throw new XMLEncryptionException("empty", nsae);
		} catch (NoSuchProviderException nspre) {
			throw new XMLEncryptionException("empty", nspre);
		} catch (NoSuchPaddingException nspae) {
			throw new XMLEncryptionException("empty", nspae);
		}

		// Now perform the encryption

		try {
			// Should internally generate an IV
			// todo - allow user to set an IV
			c.init(_cipherMode, _key);
		} catch (InvalidKeyException ike) {
			throw new XMLEncryptionException("empty", ike);
		}

        try {
            encryptedBytes =
                c.doFinal(serializedOctets.getBytes("UTF-8"));

            logger.debug("Expected cipher.outputSize = " +
                Integer.toString(c.getOutputSize(
                    serializedOctets.getBytes().length)));
            logger.debug("Actual cipher.outputSize = " +
                Integer.toString(encryptedBytes.length));
        } catch (IllegalStateException ise) {
            throw new XMLEncryptionException("empty", ise);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        } catch (UnsupportedEncodingException uee) {
		   	throw new XMLEncryptionException("empty", uee);
		}

		// Now build up to a properly XML Encryption encoded octet stream
		// IvParameterSpec iv;

		byte[] iv = c.getIV();
		byte[] finalEncryptedBytes = 
			new byte[iv.length + encryptedBytes.length];
		System.arraycopy(iv, 0, finalEncryptedBytes, 0,
						 iv.length);
		System.arraycopy(encryptedBytes, 0, finalEncryptedBytes, 
						 iv.length,
						 encryptedBytes.length);

        String base64EncodedEncryptedOctets = Base64.encode(finalEncryptedBytes);

        logger.debug("Encrypted octets:\n" + base64EncodedEncryptedOctets);
        logger.debug("Encrypted octets length = " +
            base64EncodedEncryptedOctets.length());

        try {
	    CipherData cd = _ed.getCipherData();
	    CipherValue cv = cd.getCipherValue();
	    // cv.setValue(base64EncodedEncryptedOctets.getBytes());
		cv.setValue(base64EncodedEncryptedOctets);

	    if (contentMode) {
		_ed.setType(new URI(EncryptionConstants.TYPE_CONTENT).toString());
	    }
	    else {
		_ed.setType(new URI(EncryptionConstants.TYPE_ELEMENT).toString());
	    }
            EncryptionMethod method = _factory.newEncryptionMethod(
		 new URI(_algorithm).toString());
            _ed.setEncryptionMethod(method);
        } catch (URI.MalformedURIException mfue) {
            throw new XMLEncryptionException("empty", mfue);
        }
        return (_ed);
    }

    /**
     * Returns an <code>EncryptedData</code> interface. Use this operation if
     * you want to load an <code>EncryptedData</code> structure from a DOM 
	 * structure and manipulate the contents 
     *
     * @param context the context <code>Document</code>.
     * @param element the <code>Element</code> that will be loaded
     * @throws XMLEncryptionException.
     */
    public EncryptedData loadEncryptedData(Document context, Element element) 
		throws XMLEncryptionException {
        logger.debug("Loading encrypted element...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == element)
            logger.error("Element unexpectedly null...");
        if(_cipherMode != DECRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in DECRYPT_MODE...");

        _contextDocument = context;
        _ed = _factory.newEncryptedData(element);

		return (_ed);
    }

    /**
     * Returns an <code>EncryptedKey</code> interface. Use this operation if
     * you want to load an <code>EncryptedKey</code> structure from a DOM 
	 * structure and manipulate the contents.
     *
     * @param context the context <code>Document</code>.
     * @param element the <code>Element</code> that will be loaded
     * @throws XMLEncryptionException.
     */

    public EncryptedKey loadEncryptedKey(Document context, Element element) 
		throws XMLEncryptionException {
        logger.debug("Loading encrypted key...");
        if(null == context)
            logger.error("Context document unexpectedly null...");
        if(null == element)
            logger.error("Element unexpectedly null...");
        if(_cipherMode != UNWRAP_MODE && _cipherMode != DECRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in UNWRAP_MODE or DECRYPT_MODE...");

        _contextDocument = context;
        _ek = _factory.newEncryptedKey(element);
		return (_ek);
    }

    /**
     * Returns an <code>EncryptedKey</code> interface. Use this operation if
     * you want to load an <code>EncryptedKey</code> structure from a DOM 
	 * structure and manipulate the contents.
	 *
	 * Assumes that the context document is the document that owns the element
     *
     * @param element the <code>Element</code> that will be loaded
     * @throws XMLEncryptionException.
     */

    public EncryptedKey loadEncryptedKey(Element element) 
		throws XMLEncryptionException {

		return (loadEncryptedKey(element.getOwnerDocument(), element));
    }

    /**
     * Encrypts a key to an EncryptedKey structure
	 *
	 * @param doc the Context document that will be used to general DOM
	 * @param key Key to encrypt (will use previously set KEK to 
	 * perform encryption
     */

    public EncryptedKey encryptKey(Document doc, Key key) throws
            XMLEncryptionException {

        logger.debug("Encrypting key ...");

        if(null == key) 
            logger.error("Key unexpectedly null...");
        if(_cipherMode != WRAP_MODE)
            logger.error("XMLCipher unexpectedly not in WRAP_MODE...");

		if (_algorithm == null) {

			throw new XMLEncryptionException("XMLCipher instance without transformation specified");
		}

		_contextDocument = doc;

        byte[] encryptedBytes = null;
		// Now create the working cipher

		String jceAlgorithm =
			JCEMapper.translateURItoJCEID(_algorithm).getAlgorithmID();
		String provider;

		if (_requestedJCEProvider == null)
			provider =
				JCEMapper.translateURItoJCEID(_algorithm).getProviderId();
		else
			provider = _requestedJCEProvider;

		logger.debug("provider = " + provider + "alg = " + jceAlgorithm);

		Cipher c;
		try {
			c = Cipher.getInstance(jceAlgorithm, provider);
		} catch (NoSuchAlgorithmException nsae) {
			throw new XMLEncryptionException("empty", nsae);
		} catch (NoSuchProviderException nspre) {
			throw new XMLEncryptionException("empty", nspre);
		} catch (NoSuchPaddingException nspae) {
			throw new XMLEncryptionException("empty", nspae);
		}

		// Now perform the encryption

		try {
			// Should internally generate an IV
			// todo - allow user to set an IV
			c.init(Cipher.WRAP_MODE, _key);
			encryptedBytes = c.wrap(key);
		} catch (InvalidKeyException ike) {
			throw new XMLEncryptionException("empty", ike);
		} catch (IllegalBlockSizeException ibse) {
			throw new XMLEncryptionException("empty", ibse);
		}

        String base64EncodedEncryptedOctets = Base64.encode(encryptedBytes);

        logger.debug("Encrypted key octets:\n" + base64EncodedEncryptedOctets);
        logger.debug("Encrypted key octets length = " +
            base64EncodedEncryptedOctets.length());

		CipherValue cv = _ek.getCipherData().getCipherValue();
		cv.setValue(base64EncodedEncryptedOctets);

        try {
            EncryptionMethod method = _factory.newEncryptionMethod(
                new URI(_algorithm).toString());
            _ek.setEncryptionMethod(method);
        } catch (URI.MalformedURIException mfue) {
            throw new XMLEncryptionException("empty", mfue);
        }
		return _ek;
		
    }

	/**
	 * Decrypt a key from a passed in EncryptedKey structure
	 *
	 * @param encryptedKey Previously loaded EncryptedKey that needs
	 * to be decrypted.
	 * @param keyType a URI indicated the type of key that is wrapped
	 * @returns a key corresponding to the give type
	 */

	public Key decryptKey(EncryptedKey encryptedKey, String algorithm) throws
	            XMLEncryptionException {

        logger.debug("Decrypting key from previously loaded EncryptedKey...");

        if(_cipherMode != UNWRAP_MODE)
            logger.error("XMLCipher unexpectedly not in UNWRAP_MODE...");

		if (algorithm == null) {
			throw new XMLEncryptionException("Cannot decrypt a key without knowing the algorithm");
		}

		if (_key == null) {

			logger.debug("Trying to find a KEK via key resolvers");

			KeyInfo ki = encryptedKey.getKeyInfo();
			if (ki != null) {
				try {
					_key = ki.getSecretKey();
				}
				catch (Exception e) {
				}
			}
			if (_key == null) {
				logger.error("XMLCipher::decryptKey called without a KEK and cannot resolve");
				throw new XMLEncryptionException("Unable to decrypt without a KEK");
			}
		}

		// Obtain the encrypted octets 
		XMLCipherInput cipherInput = new XMLCipherInput(encryptedKey);
		byte [] encryptedBytes = cipherInput.getBytes();

		// Now create the working cipher

		String jceAlgorithm = 
			JCEMapper.translateURItoJCEID(encryptedKey.getEncryptionMethod()
										  .getAlgorithm()).getAlgorithmID();
		String provider;

		if (_requestedJCEProvider == null)
			provider =
				JCEMapper.translateURItoJCEID(encryptedKey
											  .getEncryptionMethod()
											  .getAlgorithm())
				.getProviderId();
		else
			provider = _requestedJCEProvider;

		String jceKeyAlgorithm = 
			JCEMapper.getJCEKeyAlgorithmFromURI(algorithm, provider);
		logger.debug("JCE Provider = " + provider);
		logger.debug("JCE Algorithm = " + jceAlgorithm);

		Cipher c;
		try {
			c = Cipher.getInstance(jceAlgorithm, provider);
		} catch (NoSuchAlgorithmException nsae) {
			throw new XMLEncryptionException("empty", nsae);
		} catch (NoSuchProviderException nspre) {
			throw new XMLEncryptionException("empty", nspre);
		} catch (NoSuchPaddingException nspae) {
			throw new XMLEncryptionException("empty", nspae);
		}

		Key ret;

		try {		
			c.init(Cipher.UNWRAP_MODE, _key);
			ret = c.unwrap(encryptedBytes, jceKeyAlgorithm, Cipher.SECRET_KEY);
			
		} catch (InvalidKeyException ike) {
			throw new XMLEncryptionException("empty", ike);
		} catch (NoSuchAlgorithmException nsae) {
			throw new XMLEncryptionException("empty", nsae);
		}

		logger.info("Decryption of key type " + algorithm + " OK");

		return ret;

    }
		
	/**
	 * Decrypt a key from a passed in EncryptedKey structure.  This version
	 * is used mainly internally, when  the cipher already has an
	 * EncryptedData loaded.  The algorithm URI will be read from the 
	 * EncryptedData
	 *
	 * @param encryptedKey Previously loaded EncryptedKey that needs
	 * to be decrypted.
	 * @returns a key corresponding to the give type
	 */

	public Key decryptKey(EncryptedKey encryptedKey) throws
	            XMLEncryptionException {

		return decryptKey(encryptedKey, _ed.getEncryptionMethod().getAlgorithm());

	}

    /**
     * Removes the contents of a <code>Node</code>.
     *
     * @param node the <code>Node</code> to clear.
     */
    private void removeContent(Node node) {
        NodeList list = node.getChildNodes();
        if (list.getLength() > 0) {
            Node n = list.item(0);
            if (null != n) {
                n.getParentNode().removeChild(n);
            }
            removeContent(node);
        }
    }

    /**
     * Decrypts <code>EncryptedData</code> in a single-part operation.
     *
     * @param data the <code>EncryptedData</code> to decrypt.
     * @return the <code>Node</code> as a result of the decrypt operation.
     */
    private Document decryptElement(Element element) throws
            XMLEncryptionException {

        logger.debug("Decrypting element...");

        if(_cipherMode != DECRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in DECRYPT_MODE...");

		String octets;
		try {
			octets = new String(decryptToByteArray(element), "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			throw new XMLEncryptionException("empty", uee);
		}


        logger.debug("Decrypted octets:\n" + octets);

        Node sourceParent =  element.getParentNode();

        DocumentFragment decryptedFragment = 
			_serializer.deserialize(octets, sourceParent);


		// The de-serialiser returns a fragment whose children we need to
		// take on.

		if (sourceParent instanceof Document) {
			
		    // If this is a content decryption, this may have problems

		    _contextDocument.removeChild(_contextDocument.getDocumentElement());
		    _contextDocument.appendChild(decryptedFragment);
		}
		else {
		    sourceParent.replaceChild(decryptedFragment, element);

		}

        return (_contextDocument);
    }
    

	/**
	 * 
	 * @param element
	 */
    private Document decryptElementContent(Element element) throws 
    		XMLEncryptionException {
    	Element e = (Element) element.getElementsByTagNameNS(
    		EncryptionConstants.EncryptionSpecNS, 
    		EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);
    	
    	if (null == e) {
    		throw new XMLEncryptionException("No EncryptedData child element.");
    	}
    	
    	return (decryptElement(e));
    }

	/**
	 * Decrypt an EncryptedData element to a byte array
	 *
	 * When passed in an EncryptedData node, returns the decryption
	 * as a byte array.
	 *
	 * Does not modify the source document
	 */

	public byte[] decryptToByteArray(Element element) 
		throws XMLEncryptionException {
		
        logger.debug("Decrypting to ByteArray...");

        if(_cipherMode != DECRYPT_MODE)
            logger.error("XMLCipher unexpectedly not in DECRYPT_MODE...");

        EncryptedData encryptedData = _factory.newEncryptedData(element);

		if (_key == null) {

			KeyInfo ki = encryptedData.getKeyInfo();

			if (ki != null) {
				try {
					// Add a EncryptedKey resolver
					ki.registerInternalKeyResolver(
			             new EncryptedKeyResolver(encryptedData.
												  getEncryptionMethod().
												  getAlgorithm(), 
												  _kek));
					_key = ki.getSecretKey();
				} catch (KeyResolverException kre) {
					// We will throw in a second...
				}
			}

			if (_key == null) {
				logger.error("XMLCipher::decryptElement called without a key and unable to resolve");
				throw new XMLEncryptionException("Unable to decrypt without a key");
			}
		}

		// Obtain the encrypted octets 
		XMLCipherInput cipherInput = new XMLCipherInput(encryptedData);
		byte [] encryptedBytes = cipherInput.getBytes();

		// Now create the working cipher

		String jceAlgorithm = 
			JCEMapper.translateURItoJCEID(encryptedData.getEncryptionMethod()
										  .getAlgorithm()).getAlgorithmID();
		String provider;

		if (_requestedJCEProvider == null)
			provider =
				JCEMapper.translateURItoJCEID(encryptedData
											  .getEncryptionMethod()
											  .getAlgorithm())
				.getProviderId();
		else
			provider = _requestedJCEProvider;

		Cipher c;
		try {
			c = Cipher.getInstance(jceAlgorithm, provider);
		} catch (NoSuchAlgorithmException nsae) {
			throw new XMLEncryptionException("empty", nsae);
		} catch (NoSuchProviderException nspre) {
			throw new XMLEncryptionException("empty", nspre);
		} catch (NoSuchPaddingException nspae) {
			throw new XMLEncryptionException("empty", nspae);
		}

		// Calculate the IV length and copy out

		// For now, we only work with Block ciphers, so this will work.
		// This should probably be put into the JCE mapper.

		int ivLen = c.getBlockSize();
		byte[] ivBytes = new byte[ivLen];

		// You may be able to pass the entire piece in to IvParameterSpec
		// and it will only take the first x bytes, but no way to be certain
		// that this will work for every JCE provider, so lets copy the
		// necessary bytes into a dedicated array.

		System.arraycopy(encryptedBytes, 0, ivBytes, 0, ivLen);
		IvParameterSpec iv = new IvParameterSpec(ivBytes);		
		
		try {
			c.init(_cipherMode, _key, iv);
		} catch (InvalidKeyException ike) {
			throw new XMLEncryptionException("empty", ike);
		} catch (InvalidAlgorithmParameterException iape) {
			throw new XMLEncryptionException("empty", iape);
		}

        String octets = null;
		byte[] plainBytes;

        try {
            plainBytes = c.doFinal(encryptedBytes, 
								   ivLen, 
								   encryptedBytes.length - ivLen);

        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        }
		
        return (plainBytes);
    }
		
	/*
	 * Expose the interface for creating XML Encryption objects
	 */

    /**
     * Creates an <code>EncryptedData</code> <code>Element</code>.
     *
	 * The newEncryptedData and newEncryptedKey methods create fairly complete
	 * elements that are immediately useable.  All the other create* methods
	 * return bare elements that still need to be built upon.
	 *<p>
	 * An EncryptionMethod will still need to be added however
	 *
	 * @param type Either REFERENCE_TYPE or VALUE_TYPE - defines what kind of
	 * CipherData this EncryptedData will contain.
     * @param text the Base 64 encoded, encrypted text to wrap in the
     *   <code>EncryptedData</code> or the URI to set in the CipherReference
	 * (usage will depend on the <code>type</code>
     * @return the <code>EncryptedData</code> <code>Element</code>.
     *
     * <!--
     * <EncryptedData Id[OPT] Type[OPT] MimeType[OPT] Encoding[OPT]>
     *     <EncryptionMethod/>[OPT]
     *     <ds:KeyInfo>[OPT]
     *         <EncryptedKey/>[OPT]
     *         <AgreementMethod/>[OPT]
     *         <ds:KeyName/>[OPT]
     *         <ds:RetrievalMethod/>[OPT]
     *         <ds:[MUL]/>[OPT]
     *     </ds:KeyInfo>
     *     <CipherData>[MAN]
     *         <CipherValue/> XOR <CipherReference/>
     *     </CipherData>
     *     <EncryptionProperties/>[OPT]
     * </EncryptedData>
     * -->
     */

    public EncryptedData createEncryptedData(int type, String value) throws
            XMLEncryptionException {
        EncryptedData result = null;
        CipherData data = null;

        switch (type) {
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference = _factory.newCipherReference(
                    value);
                data = _factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = _factory.newEncryptedData(data);
				break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue = _factory.newCipherValue(value);
                data = _factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = _factory.newEncryptedData(data);
        }

        return (result);
    }

    /**
     * Creates an <code>EncryptedKey</code> <code>Element</code>.
     *
	 * The newEncryptedData and newEncryptedKey methods create fairly complete
	 * elements that are immediately useable.  All the other create* methods
	 * return bare elements that still need to be built upon.
	 *<p>
	 * An EncryptionMethod will still need to be added however
	 *
	 * @param type Either REFERENCE_TYPE or VALUE_TYPE - defines what kind of
	 * CipherData this EncryptedData will contain.
     * @param text the Base 64 encoded, encrypted text to wrap in the
     *   <code>EncryptedKey</code> or the URI to set in the CipherReference
	 * (usage will depend on the <code>type</code>
     * @return the <code>EncryptedKey</code> <code>Element</code>.
     *
     * <!--
     * <EncryptedKey Id[OPT] Type[OPT] MimeType[OPT] Encoding[OPT]>
     *     <EncryptionMethod/>[OPT]
     *     <ds:KeyInfo>[OPT]
     *         <EncryptedKey/>[OPT]
     *         <AgreementMethod/>[OPT]
     *         <ds:KeyName/>[OPT]
     *         <ds:RetrievalMethod/>[OPT]
     *         <ds:[MUL]/>[OPT]
     *     </ds:KeyInfo>
     *     <CipherData>[MAN]
     *         <CipherValue/> XOR <CipherReference/>
     *     </CipherData>
     *     <EncryptionProperties/>[OPT]
     * </EncryptedData>
     * -->
     */

    public EncryptedKey createEncryptedKey(int type, String value) throws
            XMLEncryptionException {
        EncryptedKey result = null;
        CipherData data = null;

        switch (type) {
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference = _factory.newCipherReference(
                    value);
                data = _factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = _factory.newEncryptedKey(data);
				break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue = _factory.newCipherValue(value);
                data = _factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = _factory.newEncryptedKey(data);
        }

        return (result);
    }

	/**
	 * Create an AgreementMethod object
	 *
	 * @param algorithm Algorithm of the agreement method
	 */

	public AgreementMethod createAgreementMethod(String algorithm) throws
		XMLEncryptionException {
		return (_factory.newAgreementMethod(algorithm));
	}

	/**
	 * Create a CipherData object
	 *
	 * @param type Type of this CipherData (either VALUE_TUPE or
	 * REFERENCE_TYPE)
	 */

	public CipherData createCipherData(int type) {
		return (_factory.newCipherData(type));
	}

	/**
	 * Create a CipherReference object
	 *
	 * @param uri The URI that the reference will refer to
	 */

	public CipherReference createCipherReference(String uri) throws
		XMLEncryptionException {
		return (_factory.newCipherReference(uri));
	}
	
	/**
	 * Create a CipherValue element
	 *
	 * @param value The value to set the ciphertext to
	 */

	public CipherValue createCipherValue(String value) {
		return (_factory.newCipherValue(value));
	}

	/**
	 * Create an EncryptedMethod object
	 *
	 * @param algorithm Algorithm for the encryption
	 */
	public EncryptionMethod createEncryptionMethod(String algorithm) throws
		XMLEncryptionException {
		return (_factory.newEncryptionMethod(algorithm));
	}

	/**
	 * Create an EncryptedProperties element
	 *
	 */
	public EncryptionProperties createEncryptionProperties() {
		return (_factory.newEncryptionProperties());
	}

	/**
	 * Create a new EncryptionProperty element
	 */
	public EncryptionProperty createEncryptionProperty() {
		return (_factory.newEncryptionProperty());
	}

	/**
	 * Create a new ReferenceList object
	 */
	public ReferenceList createReferenceList(int type) {
		return (new ReferenceList(type));
	}
	
	/**
	 * Create a new Transforms object
	 * <p>
	 * <b>Note</b>: A context document <i>must</i> have been set
	 * elsewhere (possibly via a call to doFinal).  If not, use the
	 * createTransforms(Document) method.
	 */

	public Transforms createTransforms() {
		return (_factory.newTransforms());
	}

	/**
	 * Create a new Transforms object
	 *
	 * Because the handling of Transforms is currently done in the signature
	 * code, the creation of a Transforms object <b>requires</b> a
	 * context document.
	 *
	 * @param doc Document that will own the created Transforms node
	 */
	public Transforms createTransforms(Document doc) {
		return (_factory.newTransforms(doc));
	}

    /**
     * Converts <code>String</code>s into <code>Node</code>s and visa versa.
     * <p>
     * <b>NOTE:</b> For internal use only.
     *
     * @author  Axl Mattheus
     */

    private class Serializer {
        private OutputFormat format;
        private XMLSerializer _serializer;

        /**
         * Initialize the <code>XMLSerializer</code> with the specified context
         * <code>Document</code>.
         *
         * @param document the context <code>Document</code>.
         */
        Serializer() {
            format = new OutputFormat();
            format.setEncoding("UTF-8");
            format.setOmitDocumentType(true);
            format.setOmitXMLDeclaration(true);
            format.setPreserveSpace(true);
        }

        /**
         * Returns a <code>String</code> representation of the specified
         * <code>Document</code>.
         *
         * @param doc the <code>Document</code> to serialize.
         * @return the <code>String</code> representation of the serilaized
         *   <code>Document</code>.
         * @throws
         */
        String serialize(Document document) throws XMLEncryptionException {
            StringWriter output = new StringWriter();
            _serializer = new XMLSerializer(output, format);

            try {
                _serializer.serialize(document);
            } catch (IOException ioe) {
                throw new XMLEncryptionException("empty", ioe);
            }

            return (output.toString());
        }

        /**
         * Returns a <code>String</code> representation of the specified
         * <code>Element</code>.
         *
         * @param doc the <code>Element</code> to serialize.
         * @return the <code>String</code> representation of the serilaized
         *   <code>Element</code>.
         * @throws XMLEncryptionException
         */
        String serialize(Element element) throws XMLEncryptionException {
            // StringWriter output = new StringWriter();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
            _serializer = new XMLSerializer(output, format);

            try {
                _serializer.serialize(element);
            } catch (IOException ioe) {
                throw new XMLEncryptionException("empty", ioe);
            }


			String ret = null;
			try {
				ret = output.toString("UTF-8");
			} catch (UnsupportedEncodingException uee) {
				throw new XMLEncryptionException("empty", uee);
			}
            return ret;
        }

        /**
         * Returns a <code>String</code> representation of the specified
         * <code>NodeList</code>.
         *
         * @param doc the <code>NodeList</code> to serialize.
         * @return the <code>String</code> representation of the serilaized
         *   <code>NodeList</code>.
         * @throws
         */
        String serialize(NodeList content) throws XMLEncryptionException {
            StringWriter output = new StringWriter();
            _serializer = new XMLSerializer(output, format);

            try {
                for (int i =0; i < content.getLength(); i++) {
                    Node n = content.item(i);
                    if (null != n) {
			int nodeType = n.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
			    _serializer.serialize((Element) n);
			}
			else if (nodeType == Node.TEXT_NODE) {
			    output.write(n.getNodeValue());
			}
                    }
                }
            } catch (IOException ioe) {
                throw new XMLEncryptionException("empty", ioe);
            }

            return (output.toString());
        }

        /**
         *
         */
        DocumentFragment deserialize(String source, Node ctx) throws XMLEncryptionException {
			DocumentFragment result;
            final String tagname = "fragment";

			// Create the context to parse the document against
			StringBuffer sb;
			
			sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><"+tagname);
			
			// Run through each node up to the document node and find any
			// xmlns: nodes

			Node wk = ctx;
			
			while (wk != null) {

				NamedNodeMap atts = wk.getAttributes();
				int length;
				if (atts != null)
					length = atts.getLength();
				else
					length = 0;

				for (int i = 0 ; i < length ; ++i) {
					Node att = atts.item(i);
					if (att.getNodeName().startsWith("xmlns:") ||
						att.getNodeName() == "xmlns") {
					
						// Check to see if this node has already been found
						Node p = ctx;
						boolean found = false;
						while (p != wk) {
							NamedNodeMap tstAtts = p.getAttributes();
							if (tstAtts != null && 
								tstAtts.getNamedItem(att.getNodeName()) != null) {
								found = true;
								break;
							}
							p = p.getParentNode();
						}
						if (found == false) {
							
							// This is an attribute node
							sb.append(" " + att.getNodeName() + "=\"" + 
									  att.getNodeValue() + "\"");
						}
					}
				}
				wk = wk.getParentNode();
			}
			sb.append(">" + source + "</" + tagname + ">");
			String fragment = sb.toString();

            try {
                DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document d = db.parse(
				    new InputSource(new StringReader(fragment)));

				Element fragElt = (Element) _contextDocument.importNode(
						 d.getDocumentElement(), true);
				result = _contextDocument.createDocumentFragment();
				Node child = fragElt.getFirstChild();
				while (child != null) {
					fragElt.removeChild(child);
					result.appendChild(child);
					child = fragElt.getFirstChild();
				}
				// String outp = serialize(d);

            } catch (SAXException se) {
                throw new XMLEncryptionException("empty", se);
            } catch (ParserConfigurationException pce) {
                throw new XMLEncryptionException("empty", pce);
            } catch (IOException ioe) {
                throw new XMLEncryptionException("empty", ioe);
            }

            return (result);
        }
    }


    /**
     *
     * @author Axl Mattheus
     */
    private class Factory {
        /**
         *
         */
        AgreementMethod newAgreementMethod(String algorithm) throws
                XMLEncryptionException {
            return (new AgreementMethodImpl(algorithm));
        }

        /**
         *
         */
        CipherData newCipherData(int type) {
            return (new CipherDataImpl(type));
        }

        /**
         *
         */
        CipherReference newCipherReference(String uri) throws
                XMLEncryptionException {
            return (new CipherReferenceImpl(uri));
        }

        /**
         *
         */
        CipherValue newCipherValue(String value) {
            return (new CipherValueImpl(value));
        }

        /**
         *
         
        CipherValue newCipherValue(byte[] value) {
            return (new CipherValueImpl(value));
        }
		*/
        /**
         *
         */
        EncryptedData newEncryptedData(CipherData data) {
            return (new EncryptedDataImpl(data));
        }

        /**
         *
         */
        EncryptedKey newEncryptedKey(CipherData data) {
            return (new EncryptedKeyImpl(data));
        }

        /**
         *
         */
        EncryptionMethod newEncryptionMethod(String algorithm) throws
                XMLEncryptionException {
            return (new EncryptionMethodImpl(algorithm));
        }

        /**
         *
         */
        EncryptionProperties newEncryptionProperties() {
            return (new EncryptionPropertiesImpl());
        }

        /**
         *
         */
        EncryptionProperty newEncryptionProperty() {
            return (new EncryptionPropertyImpl());
        }

        /**
         *
         */
        ReferenceList newReferenceList(int type) {
            return (new ReferenceList(type));
        }

        /**
         *
         */
        Transforms newTransforms() {
            return (new TransformsImpl());
        }

        /**
         *
         */
        Transforms newTransforms(Document doc) {
            return (new TransformsImpl(doc));
        }

        /**
         *
         */
        // <element name="AgreementMethod" type="xenc:AgreementMethodType"/>
        // <complexType name="AgreementMethodType" mixed="true">
        //     <sequence>
        //         <element name="KA-Nonce" minOccurs="0" type="base64Binary"/>
        //         <!-- <element ref="ds:DigestMethod" minOccurs="0"/> -->
        //         <any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
        //         <element name="OriginatorKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
        //         <element name="RecipientKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
        //     </sequence>
        //     <attribute name="Algorithm" type="anyURI" use="required"/>
        // </complexType>
        AgreementMethod newAgreementMethod(Element element) throws
                XMLEncryptionException {
            if (null == element) {
                //complain
            }

            String algorithm = element.getAttributeNS(null, 
            	EncryptionConstants._ATT_ALGORITHM);
            AgreementMethod result = newAgreementMethod(algorithm);

            Element kaNonceElement = (Element) element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_KA_NONCE).item(0);
            if (null != kaNonceElement) {
                result.setKANonce(kaNonceElement.getNodeValue().getBytes());
            }
            // TODO: ///////////////////////////////////////////////////////////
            // Figure out how to make this pesky line work..
            // <any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>

            // TODO: ///////////////////////////////////////////////////////////
            // Implement properly, implement a KeyInfo marshaler.
            Element originatorKeyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ORIGINATORKEYINFO).item(0);
            if (null != originatorKeyInfoElement) {
                result.setOriginatorKeyInfo(null);
            }

            // TODO: ///////////////////////////////////////////////////////////
            // Implement properly, implement a KeyInfo marshaler.
            Element recipientKeyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_RECIPIENTKEYINFO).item(0);
            if (null != recipientKeyInfoElement) {
                result.setRecipientKeyInfo(null);
            }

            return (result);
        }

        /**
         *
         */
        // <element name='CipherData' type='xenc:CipherDataType'/>
        // <complexType name='CipherDataType'>
        //     <choice>
        //         <element name='CipherValue' type='base64Binary'/>
        //         <element ref='xenc:CipherReference'/>
        //     </choice>
        // </complexType>
        CipherData newCipherData(Element element) throws
                XMLEncryptionException {
            if (null == element) {
                // complain
            }

            int type = 0;
            Element e = null;
            if (element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS, 
                EncryptionConstants._TAG_CIPHERVALUE).getLength() > 0) {
                type = CipherData.VALUE_TYPE;
                e = (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERVALUE).item(0);
            } else if (element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_CIPHERREFERENCE).getLength() > 0) {
                type = CipherData.REFERENCE_TYPE;
                e = (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERREFERENCE).item(0);
            }

            CipherData result = newCipherData(type);
            if (type == CipherData.VALUE_TYPE) {
                result.setCipherValue(newCipherValue(e));
            } else if (type == CipherData.REFERENCE_TYPE) {
                result.setCipherReference(newCipherReference(e));
            }

            return (result);
        }

        /**
         *
         */
        // <element name='CipherReference' type='xenc:CipherReferenceType'/>
        // <complexType name='CipherReferenceType'>
        //     <sequence>
        //         <element name='Transforms' type='xenc:TransformsType' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='URI' type='anyURI' use='required'/>
        // </complexType>
        CipherReference newCipherReference(Element element) throws
                XMLEncryptionException {

			Attr URIAttr = 
				element.getAttributeNodeNS(null, EncryptionConstants._ATT_URI);
			CipherReference result = new CipherReferenceImpl(URIAttr);

			// Find any Transforms

			NodeList transformsElements = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_TRANSFORMS);
            Element transformsElement =
				(Element) transformsElements.item(0);
			
			if (transformsElement != null) {
				logger.debug("Creating a DSIG based Transforms element");
				try {
					result.setTransforms(new TransformsImpl(transformsElement));
				}
				catch (XMLSignatureException xse) {
					throw new XMLEncryptionException("empty", xse);
				} catch (InvalidTransformException ite) {
					throw new XMLEncryptionException("empty", ite);
				} catch (XMLSecurityException xse) {
					throw new XMLEncryptionException("empty", xse);
				}

			}

			return result;
        }

        /**
         *
         */
        CipherValue newCipherValue(Element element) throws
                XMLEncryptionException {
            String value = element.getFirstChild().getNodeValue();

            CipherValue result = newCipherValue(value);

            return (result);
        }

        /**
         *
         */
        // <complexType name='EncryptedType' abstract='true'>
        //     <sequence>
        //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
        //             minOccurs='0'/>
        //         <element ref='ds:KeyInfo' minOccurs='0'/>
        //         <element ref='xenc:CipherData'/>
        //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <attribute name='Type' type='anyURI' use='optional'/>
        //     <attribute name='MimeType' type='string' use='optional'/>
        //     <attribute name='Encoding' type='anyURI' use='optional'/>
        // </complexType>
        // <element name='EncryptedData' type='xenc:EncryptedDataType'/>
        // <complexType name='EncryptedDataType'>
        //     <complexContent>
        //         <extension base='xenc:EncryptedType'/>
        //     </complexContent>
        // </complexType>
        EncryptedData newEncryptedData(Element element) throws
			XMLEncryptionException {
            EncryptedData result = null;

			NodeList dataElements = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERDATA);

			// Need to get the last CipherData found, as earlier ones will
			// be for elements in the KeyInfo lists

            Element dataElement =
				(Element) dataElements.item(dataElements.getLength() - 1);

            CipherData data = newCipherData(dataElement);

            result = newEncryptedData(data);

            try {
                result.setId(element.getAttributeNS(
                    null, EncryptionConstants._ATT_ID));
                result.setType(new URI(
                    element.getAttributeNS(
                        null, EncryptionConstants._ATT_TYPE)).toString());
                result.setMimeType(element.getAttributeNS(
                    null, EncryptionConstants._ATT_MIMETYPE));
                result.setEncoding(new URI(
                    element.getAttributeNS(
                        null, Constants._ATT_ENCODING)).toString());
            } catch (URI.MalformedURIException mfue) {
                // do nothing
            }

            Element encryptionMethodElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(newEncryptionMethod(
                    encryptionMethodElement));
            }

            // BFL 16/7/03 - simple implementation
			// TODO: Work out how to handle relative URI

            Element keyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    Constants.SignatureSpecNS, Constants._TAG_KEYINFO).item(0);
            if (null != keyInfoElement) {
				try {
					result.setKeyInfo(new KeyInfo(keyInfoElement, null));
				} catch (XMLSecurityException xse) {
					throw new XMLEncryptionException("Error loading Key Info", 
													 xse);
				}
            }

            // TODO: Implement
            Element encryptionPropertiesElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if (null != encryptionPropertiesElement) {
                result.setEncryptionProperties(
                    newEncryptionProperties(encryptionPropertiesElement));
            }

            return (result);
        }

        /**
         *
         */
        // <complexType name='EncryptedType' abstract='true'>
        //     <sequence>
        //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
        //             minOccurs='0'/>
        //         <element ref='ds:KeyInfo' minOccurs='0'/>
        //         <element ref='xenc:CipherData'/>
        //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <attribute name='Type' type='anyURI' use='optional'/>
        //     <attribute name='MimeType' type='string' use='optional'/>
        //     <attribute name='Encoding' type='anyURI' use='optional'/>
        // </complexType>
        // <element name='EncryptedKey' type='xenc:EncryptedKeyType'/>
        // <complexType name='EncryptedKeyType'>
        //     <complexContent>
        //         <extension base='xenc:EncryptedType'>
        //             <sequence>
        //                 <element ref='xenc:ReferenceList' minOccurs='0'/>
        //                 <element name='CarriedKeyName' type='string' minOccurs='0'/>
        //             </sequence>
        //             <attribute name='Recipient' type='string' use='optional'/>
        //         </extension>
        //     </complexContent>
        // </complexType>
        EncryptedKey newEncryptedKey(Element element) throws
                XMLEncryptionException {
            EncryptedKey result = null;
			NodeList dataElements = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERDATA);
            Element dataElement =
				(Element) dataElements.item(dataElements.getLength() - 1);

            CipherData data = newCipherData(dataElement);
            result = newEncryptedKey(data);

            try {
                result.setId(element.getAttributeNS(
                    null, EncryptionConstants._ATT_ID));
                result.setType(new URI(
                    element.getAttributeNS(
                        null, EncryptionConstants._ATT_TYPE)).toString());
                result.setMimeType(element.getAttributeNS(
                    null, EncryptionConstants._ATT_MIMETYPE));
                result.setEncoding(new URI(
                    element.getAttributeNS(
                        null, Constants._ATT_ENCODING)).toString());
                result.setRecipient(element.getAttributeNS(
                    null, EncryptionConstants._ATT_RECIPIENT));
            } catch (URI.MalformedURIException mfue) {
                // do nothing
            }

            Element encryptionMethodElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(newEncryptionMethod(
                    encryptionMethodElement));
            }

            Element keyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    Constants.SignatureSpecNS, Constants._TAG_KEYINFO).item(0);
            if (null != keyInfoElement) {
				try {
					result.setKeyInfo(new KeyInfo(keyInfoElement, null));
				} catch (XMLSecurityException xse) {
					throw new XMLEncryptionException("Error loading Key Info", 
													 xse);
				}
            }

            // TODO: Implement
            Element encryptionPropertiesElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if (null != encryptionPropertiesElement) {
                result.setEncryptionProperties(
                    newEncryptionProperties(encryptionPropertiesElement));
            }

            Element referenceListElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_REFERENCELIST).item(0);
            if (null != referenceListElement) {
                result.setReferenceList(newReferenceList(referenceListElement));
            }

            Element carriedNameElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CARRIEDKEYNAME).item(0);
            if (null != carriedNameElement) {
                result.setCarriedName(carriedNameElement.getNodeValue());
            }

            return (result);
        }

        /**
         *
         */
        // <complexType name='EncryptionMethodType' mixed='true'>
        //     <sequence>
        //         <element name='KeySize' minOccurs='0' type='xenc:KeySizeType'/>
        //         <element name='OAEPparams' minOccurs='0' type='base64Binary'/>
        //         <any namespace='##other' minOccurs='0' maxOccurs='unbounded'/>
        //     </sequence>
        //     <attribute name='Algorithm' type='anyURI' use='required'/>
        // </complexType>
        EncryptionMethod newEncryptionMethod(Element element) throws
                XMLEncryptionException {
            String algorithm = element.getAttributeNS(
                null, EncryptionConstants._ATT_ALGORITHM);
            EncryptionMethod result = newEncryptionMethod(algorithm);

            Element keySizeElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_KEYSIZE).item(0);
            if (null != keySizeElement) {
                result.setKeySize(
                    Integer.valueOf(keySizeElement.getNodeValue()).intValue());
            }

            Element oaepParamsElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_OAEPPARAMS).item(0);
            if (null != oaepParamsElement) {
                result.setOAEPparams(
                    oaepParamsElement.getNodeValue().getBytes());
            }

            // TODO: Make this mess work
            // <any namespace='##other' minOccurs='0' maxOccurs='unbounded'/>

            return (result);
        }

        /**
         *
         */
        // <element name='EncryptionProperties' type='xenc:EncryptionPropertiesType'/>
        // <complexType name='EncryptionPropertiesType'>
        //     <sequence>
        //         <element ref='xenc:EncryptionProperty' maxOccurs='unbounded'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        // </complexType>
        EncryptionProperties newEncryptionProperties(Element element) throws
                XMLEncryptionException {
            EncryptionProperties result = newEncryptionProperties();

            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));

            NodeList encryptionPropertyList =
                element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTY);
            for(int i = 0; i < encryptionPropertyList.getLength(); i++) {
                Node n = encryptionPropertyList.item(i);
                if (null != n) {
                    result.addEncryptionProperty(
                        newEncryptionProperty((Element) n));
                }
            }

            return (result);
        }

        /**
         *
         */
        // <element name='EncryptionProperty' type='xenc:EncryptionPropertyType'/>
        // <complexType name='EncryptionPropertyType' mixed='true'>
        //     <choice maxOccurs='unbounded'>
        //         <any namespace='##other' processContents='lax'/>
        //     </choice>
        //     <attribute name='Target' type='anyURI' use='optional'/>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <anyAttribute namespace="http://www.w3.org/XML/1998/namespace"/>
        // </complexType>
        EncryptionProperty newEncryptionProperty(Element element) throws
                XMLEncryptionException {
            EncryptionProperty result = newEncryptionProperty();

            try {
                result.setTarget(new URI(
                    element.getAttributeNS(
                        null, EncryptionConstants._ATT_TARGET)).toString());
            } catch (URI.MalformedURIException mfue) {
                // do nothing
            }
            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));
            // TODO: Make this lot work...
            // <anyAttribute namespace="http://www.w3.org/XML/1998/namespace"/>

            // TODO: Make this work...
            // <any namespace='##other' processContents='lax'/>

            return (result);
        }

        /**
         *
         */
        // <element name='ReferenceList'>
        //     <complexType>
        //         <choice minOccurs='1' maxOccurs='unbounded'>
        //             <element name='DataReference' type='xenc:ReferenceType'/>
        //             <element name='KeyReference' type='xenc:ReferenceType'/>
        //         </choice>
        //     </complexType>
        // </element>
        ReferenceList newReferenceList(Element element) throws
                XMLEncryptionException {
            int type = 0;
            if (null != element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS, 
                EncryptionConstants._TAG_DATAREFERENCE).item(0)) {
                type = ReferenceList.DATA_REFERENCE;
            } else if (null != element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_KEYREFERENCE).item(0)) {
                type = ReferenceList.KEY_REFERENCE;
            } else {
                // complain
            }

            ReferenceList result = newReferenceList(type);
            NodeList list = null;
            switch (type) {
                case ReferenceList.DATA_REFERENCE:
                list = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_DATAREFERENCE);
                for (int i = 0; i < list.getLength() ; i++) {
                    String uri = null;
                    try {
                        uri = new URI(
                            ((Element) list.item(0)).getNodeValue()).toString();
                    } catch (URI.MalformedURIException mfue) {
                    }
                    result.add(ReferenceList.newDataReference(uri));
                }
                case ReferenceList.KEY_REFERENCE:
                list = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_KEYREFERENCE);
                for (int i = 0; i < list.getLength() ; i++) {
                    String uri = null;
                    try {
                        uri = new URI(
                            ((Element) list.item(0)).getNodeValue()).toString();
                    } catch (URI.MalformedURIException mfue) {
                    }
                    result.add(ReferenceList.newKeyReference(uri));
                }
            }

            return (result);
        }

        /**
         *
         */
        Transforms newTransforms(Element element) {
            return (null);
        }

        /**
         *
         */
        Element toElement(AgreementMethod agreementMethod) {
            return ((AgreementMethodImpl) agreementMethod).toElement();
        }

        /**
         *
         */
        Element toElement(CipherData cipherData) {
            return ((CipherDataImpl) cipherData).toElement();
        }

        /**
         *
         */
        Element toElement(CipherReference cipherReference) {
            return ((CipherReferenceImpl) cipherReference).toElement();
        }

        /**
         *
         */
        Element toElement(CipherValue cipherValue) {
            return ((CipherValueImpl) cipherValue).toElement();
        }

        /**
         *
         */
        Element toElement(EncryptedData encryptedData) {
            return ((EncryptedDataImpl) encryptedData).toElement();
        }

        /**
         *
         */
        Element toElement(EncryptedKey encryptedKey) {
            return ((EncryptedKeyImpl) encryptedKey).toElement();
        }

        /**
         *
         */
        Element toElement(EncryptionMethod encryptionMethod) {
            return ((EncryptionMethodImpl) encryptionMethod).toElement();
        }

        /**
         *
         */
        Element toElement(EncryptionProperties encryptionProperties) {
            return ((EncryptionPropertiesImpl) encryptionProperties).toElement();
        }

        /**
         *
         */
        Element toElement(EncryptionProperty encryptionProperty) {
            return ((EncryptionPropertyImpl) encryptionProperty).toElement();
        }

        Element toElement(ReferenceList referenceList) {
            // NOTE: ///////////////////////////////////////////////////////////
            // TODO: Complete
            return (null);
        }

        /**
         *
         */
        Element toElement(Transforms transforms) {
            return ((TransformsImpl) transforms).toElement();
        }

        // <element name="AgreementMethod" type="xenc:AgreementMethodType"/>
        // <complexType name="AgreementMethodType" mixed="true">
        //     <sequence>
        //         <element name="KA-Nonce" minOccurs="0" type="base64Binary"/>
        //         <!-- <element ref="ds:DigestMethod" minOccurs="0"/> -->
        //         <any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
        //         <element name="OriginatorKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
        //         <element name="RecipientKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
        //     </sequence>
        //     <attribute name="Algorithm" type="anyURI" use="required"/>
        // </complexType>
        private class AgreementMethodImpl implements AgreementMethod {
            private byte[] kaNonce = null;
            private List agreementMethodInformation = null;
            private KeyInfo originatorKeyInfo = null;
            private KeyInfo recipientKeyInfo = null;
            private String algorithmURI = null;

            public AgreementMethodImpl(String algorithm) {
                agreementMethodInformation = new LinkedList();
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException fmue) {
                    //complain?
                }
                algorithmURI = tmpAlgorithm.toString();
            }

            public byte[] getKANonce() {
                return (kaNonce);
            }

            public void setKANonce(byte[] kanonce) {
                kaNonce = kanonce;
            }

            public Iterator getAgreementMethodInformation() {
                return (agreementMethodInformation.iterator());
            }

            public void addAgreementMethodInformation(Element info) {
                agreementMethodInformation.add(info);
            }

            public void revoveAgreementMethodInformation(Element info) {
                agreementMethodInformation.remove(info);
            }

            public KeyInfo getOriginatorKeyInfo() {
                return (originatorKeyInfo);
            }

            public void setOriginatorKeyInfo(KeyInfo keyInfo) {
                originatorKeyInfo = keyInfo;
            }

            public KeyInfo getRecipientKeyInfo() {
                return (recipientKeyInfo);
            }

            public void setRecipientKeyInfo(KeyInfo keyInfo) {
                recipientKeyInfo = keyInfo;
            }

            public String getAlgorithm() {
                return (algorithmURI);
            }

            public void setAlgorithm(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException mfue) {
                    //complain
                }
                algorithm = tmpAlgorithm.toString();
            }

            // <element name="AgreementMethod" type="xenc:AgreementMethodType"/>
            // <complexType name="AgreementMethodType" mixed="true">
            //     <sequence>
            //         <element name="KA-Nonce" minOccurs="0" type="base64Binary"/>
            //         <!-- <element ref="ds:DigestMethod" minOccurs="0"/> -->
            //         <any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
            //         <element name="OriginatorKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
            //         <element name="RecipientKeyInfo" minOccurs="0" type="ds:KeyInfoType"/>
            //     </sequence>
            //     <attribute name="Algorithm" type="anyURI" use="required"/>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, 
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_AGREEMENTMETHOD);
                result.setAttributeNS(
                    null, EncryptionConstants._ATT_ALGORITHM, algorithmURI);
                if (null != kaNonce) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(
                            _contextDocument, 
                            EncryptionConstants.EncryptionSpecNS, 
                            EncryptionConstants._TAG_KA_NONCE)).appendChild(
                            _contextDocument.createTextNode(new String(kaNonce)));
                }
                if (!agreementMethodInformation.isEmpty()) {
                    Iterator itr = agreementMethodInformation.iterator();
                    while (itr.hasNext()) {
                        result.appendChild((Element) itr.next());
                    }
                }
                if (null != originatorKeyInfo) {
                    // TODO: complete
                }
                if (null != recipientKeyInfo) {
                    // TODO: complete
                }

                return (result);
            }
        }

        // <element name='CipherData' type='xenc:CipherDataType'/>
        // <complexType name='CipherDataType'>
        //     <choice>
        //         <element name='CipherValue' type='base64Binary'/>
        //         <element ref='xenc:CipherReference'/>
        //     </choice>
        // </complexType>
        private class CipherDataImpl implements CipherData {
            private static final String valueMessage =
                "Data type is reference type.";
            private static final String referenceMessage =
                "Data type is value type.";
            private CipherValue cipherValue = null;
            private CipherReference cipherReference = null;
            private int cipherType = Integer.MIN_VALUE;

            public CipherDataImpl(int type) {
                cipherType = type;
            }

            public CipherValue getCipherValue() {
                return (cipherValue);
            }

            public void setCipherValue(CipherValue value) throws
                    XMLEncryptionException {

                if (cipherType == REFERENCE_TYPE) {
                    throw new XMLEncryptionException("empty",
                        new UnsupportedOperationException(valueMessage));
                }

                cipherValue = value;
            }

            public CipherReference getCipherReference() {
                return (cipherReference);
            }

            public void setCipherReference(CipherReference reference) throws
                    XMLEncryptionException {
                if (cipherType == VALUE_TYPE) {
                    throw new XMLEncryptionException("empty",
                        new UnsupportedOperationException(referenceMessage));
                }

                cipherReference = reference;
            }

            public int getDataType() {
                return (cipherType);
            }

            // <element name='CipherData' type='xenc:CipherDataType'/>
            // <complexType name='CipherDataType'>
            //     <choice>
            //         <element name='CipherValue' type='base64Binary'/>
            //         <element ref='xenc:CipherReference'/>
            //     </choice>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, 
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_CIPHERDATA);
                if (cipherType == VALUE_TYPE) {
                    result.appendChild(
                        ((CipherValueImpl) cipherValue).toElement());
                } else if (cipherType == REFERENCE_TYPE) {
                    result.appendChild(
                        ((CipherReferenceImpl) cipherReference).toElement());
                } else {
                    // complain
                }

                return (result);
            }
        }

        // <element name='CipherReference' type='xenc:CipherReferenceType'/>
        // <complexType name='CipherReferenceType'>
        //     <sequence>
        //         <element name='Transforms' type='xenc:TransformsType' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='URI' type='anyURI' use='required'/>
        // </complexType>
        private class CipherReferenceImpl implements CipherReference {
            private String referenceURI = null;
            private Transforms referenceTransforms = null;
			private Attr referenceNode = null;

            public CipherReferenceImpl(String uri) {
				/* Don't check validity of URI as may be "" */
                referenceURI = uri;
				referenceNode = null;
            }

			public CipherReferenceImpl(Attr uri) {
				referenceURI = uri.getNodeValue();
				referenceNode = uri;
			}

            public String getURI() {
                return (referenceURI);
            }

			public Attr getURIAsAttr() {
				return (referenceNode);
			}

            public Transforms getTransforms() {
                return (referenceTransforms);
            }

            public void setTransforms(Transforms transforms) {
                referenceTransforms = transforms;
            }

            // <element name='CipherReference' type='xenc:CipherReferenceType'/>
            // <complexType name='CipherReferenceType'>
            //     <sequence>
            //         <element name='Transforms' type='xenc:TransformsType' minOccurs='0'/>
            //     </sequence>
            //     <attribute name='URI' type='anyURI' use='required'/>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, 
                    EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_CIPHERREFERENCE);
                result.setAttributeNS(
                    null, EncryptionConstants._ATT_URI, referenceURI);
                if (null != referenceTransforms) {
                    result.appendChild(
                        ((TransformsImpl) referenceTransforms).toElement());
                }

                return (result);
            }
        }

        private class CipherValueImpl implements CipherValue {
			private String cipherValue = null;
			
            // public CipherValueImpl(byte[] value) {
               // cipherValue = value;
            // }

            public CipherValueImpl(String value) {
				// cipherValue = value.getBytes();
				cipherValue = value;
            }

			// public byte[] getValue() {
			public String getValue() {
                return (cipherValue);
            }

			// public void setValue(byte[] value) {
			// public void setValue(String value) {
               // cipherValue = value;
            // }

            public void setValue(String value) {
                // cipherValue = value.getBytes();
				cipherValue = value;
            }

            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_CIPHERVALUE);
                result.appendChild(_contextDocument.createTextNode(
                    new String(cipherValue)));

                return (result);
            }
        }

        // <complexType name='EncryptedType' abstract='true'>
        //     <sequence>
        //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
        //             minOccurs='0'/>
        //         <element ref='ds:KeyInfo' minOccurs='0'/>
        //         <element ref='xenc:CipherData'/>
        //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <attribute name='Type' type='anyURI' use='optional'/>
        //     <attribute name='MimeType' type='string' use='optional'/>
        //     <attribute name='Encoding' type='anyURI' use='optional'/>
        // </complexType>
        // <element name='EncryptedData' type='xenc:EncryptedDataType'/>
        // <complexType name='EncryptedDataType'>
        //     <complexContent>
        //         <extension base='xenc:EncryptedType'/>
        //     </complexContent>
        // </complexType>
        private class EncryptedDataImpl extends EncryptedTypeImpl implements
                EncryptedData {
            public EncryptedDataImpl(CipherData data) {
                super(data);
            }

            // <complexType name='EncryptedType' abstract='true'>
            //     <sequence>
            //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
            //             minOccurs='0'/>
            //         <element ref='ds:KeyInfo' minOccurs='0'/>
            //         <element ref='xenc:CipherData'/>
            //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
            //     </sequence>
            //     <attribute name='Id' type='ID' use='optional'/>
            //     <attribute name='Type' type='anyURI' use='optional'/>
            //     <attribute name='MimeType' type='string' use='optional'/>
            //     <attribute name='Encoding' type='anyURI' use='optional'/>
            // </complexType>
            // <element name='EncryptedData' type='xenc:EncryptedDataType'/>
            // <complexType name='EncryptedDataType'>
            //     <complexContent>
            //         <extension base='xenc:EncryptedType'/>
            //     </complexContent>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTEDDATA);

                if (null != super.getId()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ID, super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_TYPE,
                        super.getType().toString());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_MIMETYPE, 
                        super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ENCODING, 
                        super.getEncoding().toString());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)
                        super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    result.appendChild(super.getKeyInfo().getElement());
                }

                result.appendChild(
                    ((CipherDataImpl) super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)
                        super.getEncryptionProperties()).toElement());
                }

                return (result);
            }
        }

        // <complexType name='EncryptedType' abstract='true'>
        //     <sequence>
        //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
        //             minOccurs='0'/>
        //         <element ref='ds:KeyInfo' minOccurs='0'/>
        //         <element ref='xenc:CipherData'/>
        //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <attribute name='Type' type='anyURI' use='optional'/>
        //     <attribute name='MimeType' type='string' use='optional'/>
        //     <attribute name='Encoding' type='anyURI' use='optional'/>
        // </complexType>
        // <element name='EncryptedKey' type='xenc:EncryptedKeyType'/>
        // <complexType name='EncryptedKeyType'>
        //     <complexContent>
        //         <extension base='xenc:EncryptedType'>
        //             <sequence>
        //                 <element ref='xenc:ReferenceList' minOccurs='0'/>
        //                 <element name='CarriedKeyName' type='string' minOccurs='0'/>
        //             </sequence>
        //             <attribute name='Recipient' type='string' use='optional'/>
        //         </extension>
        //     </complexContent>
        // </complexType>
        private class EncryptedKeyImpl extends EncryptedTypeImpl implements
                EncryptedKey {
            private String keyRecipient = null;
            private ReferenceList referenceList = null;
            private String carriedName = null;

            public EncryptedKeyImpl(CipherData data) {
                super(data);
            }

            public String getRecipient() {
                return (keyRecipient);
            }

            public void setRecipient(String recipient) {
                keyRecipient = recipient;
            }

            public ReferenceList getReferenceList() {
                return (referenceList);
            }

            public void setReferenceList(ReferenceList list) {
                referenceList = list;
            }

            public String getCarriedName() {
                return (carriedName);
            }

            public void setCarriedName(String name) {
                carriedName = name;
            }

            // <complexType name='EncryptedType' abstract='true'>
            //     <sequence>
            //         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
            //             minOccurs='0'/>
            //         <element ref='ds:KeyInfo' minOccurs='0'/>
            //         <element ref='xenc:CipherData'/>
            //         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
            //     </sequence>
            //     <attribute name='Id' type='ID' use='optional'/>
            //     <attribute name='Type' type='anyURI' use='optional'/>
            //     <attribute name='MimeType' type='string' use='optional'/>
            //     <attribute name='Encoding' type='anyURI' use='optional'/>
            // </complexType>
            // <element name='EncryptedKey' type='xenc:EncryptedKeyType'/>
            // <complexType name='EncryptedKeyType'>
            //     <complexContent>
            //         <extension base='xenc:EncryptedType'>
            //             <sequence>
            //                 <element ref='xenc:ReferenceList' minOccurs='0'/>
            //                 <element name='CarriedKeyName' type='string' minOccurs='0'/>
            //             </sequence>
            //             <attribute name='Recipient' type='string' use='optional'/>
            //         </extension>
            //     </complexContent>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTEDKEY);

                if (null != super.getId()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ID, super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_TYPE, 
                        super.getType().toString());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(null, 
                        EncryptionConstants._ATT_MIMETYPE, super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(null, Constants._ATT_ENCODING,
                        super.getEncoding().toString());
                }
                if (null != getRecipient()) {
                    result.setAttributeNS(null, 
                        EncryptionConstants._ATT_RECIPIENT, getRecipient());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)
                        super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    // TODO: complete
                }
                result.appendChild(
                    ((CipherDataImpl) super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)
                        super.getEncryptionProperties()).toElement());
                }
                if (referenceList != null && !referenceList.isEmpty()) {
                    // TODO: complete
                }
                if (null != carriedName) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(_contextDocument, 
                            EncryptionConstants.EncryptionSpecNS, 
                            EncryptionConstants._TAG_CARRIEDKEYNAME).appendChild(
                            _contextDocument.createTextNode(carriedName)));
                }

                return (result);
            }
        }

        private abstract class EncryptedTypeImpl {
            private String id =  null;
            private String type = null;
            private String mimeType = null;
            private String encoding = null;
            private EncryptionMethod encryptionMethod = null;
            private KeyInfo keyInfo = null;
            private CipherData cipherData = null;
            private EncryptionProperties encryptionProperties = null;

            protected EncryptedTypeImpl(CipherData data) {
                cipherData = data;
            }

            public String getId() {
                return (id);
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getType() {
                return (type);
            }

            public void setType(String type) {
                URI tmpType = null;
                try {
                    tmpType = new URI(type);
                } catch (URI.MalformedURIException mfue) {
                    // complain
                }
                this.type = tmpType.toString();
            }

            public String getMimeType() {
                return (mimeType);
            }

            public void setMimeType(String type) {
                mimeType = type;
            }

            public String getEncoding() {
                return (encoding);
            }

            public void setEncoding(String encoding) {
                URI tmpEncoding = null;
                try {
                    tmpEncoding = new URI(encoding);
                } catch (URI.MalformedURIException mfue) {
                    // complain
                }
                this.encoding = tmpEncoding.toString();
            }

            public EncryptionMethod getEncryptionMethod() {
                return (encryptionMethod);
            }

            public void setEncryptionMethod(EncryptionMethod method) {
                encryptionMethod = method;
            }

            public KeyInfo getKeyInfo() {
                return (keyInfo);
            }

            public void setKeyInfo(KeyInfo info) {
                keyInfo = info;
            }

            public CipherData getCipherData() {
                return (cipherData);
            }

            public EncryptionProperties getEncryptionProperties() {
                return (encryptionProperties);
            }

            public void setEncryptionProperties(
                    EncryptionProperties properties) {
                encryptionProperties = properties;
            }
        }

        // <complexType name='EncryptionMethodType' mixed='true'>
        //     <sequence>
        //         <element name='KeySize' minOccurs='0' type='xenc:KeySizeType'/>
        //         <element name='OAEPparams' minOccurs='0' type='base64Binary'/>
        //         <any namespace='##other' minOccurs='0' maxOccurs='unbounded'/>
        //     </sequence>
        //     <attribute name='Algorithm' type='anyURI' use='required'/>
        // </complexType>
        private class EncryptionMethodImpl implements EncryptionMethod {
            private String algorithm = null;
            private int keySize = Integer.MIN_VALUE;
            private byte[] oaepParams = null;
            private List encryptionMethodInformation = null;

            public EncryptionMethodImpl(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException mfue) {
                    // complain
                }
                this.algorithm = tmpAlgorithm.toString();
                encryptionMethodInformation = new LinkedList();
            }

            public String getAlgorithm() {
                return (algorithm);
            }

            public int getKeySize() {
                return (keySize);
            }

            public void setKeySize(int size) {
                keySize = size;
            }

            public byte[] getOAEPparams() {
                return (oaepParams);
            }

            public void setOAEPparams(byte[] params) {
                oaepParams = params;
            }

            public Iterator getEncryptionMethodInformation() {
                return (encryptionMethodInformation.iterator());
            }

            public void addEncryptionMethodInformation(Element info) {
                encryptionMethodInformation.add(info);
            }

            public void removeEncryptionMethodInformation(Element info) {
                encryptionMethodInformation.remove(info);
            }

            // <complexType name='EncryptionMethodType' mixed='true'>
            //     <sequence>
            //         <element name='KeySize' minOccurs='0' type='xenc:KeySizeType'/>
            //         <element name='OAEPparams' minOccurs='0' type='base64Binary'/>
            //         <any namespace='##other' minOccurs='0' maxOccurs='unbounded'/>
            //     </sequence>
            //     <attribute name='Algorithm' type='anyURI' use='required'/>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD);
                result.setAttributeNS(null, EncryptionConstants._ATT_ALGORITHM, 
                    algorithm.toString());
                if (keySize > 0) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(_contextDocument, 
                            EncryptionConstants.EncryptionSpecNS, 
                            EncryptionConstants._TAG_KEYSIZE).appendChild(
                            _contextDocument.createTextNode(
                                String.valueOf(keySize))));
                }
                if (null != oaepParams) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(_contextDocument, 
                            EncryptionConstants.EncryptionSpecNS, 
                            EncryptionConstants._TAG_OAEPPARAMS).appendChild(
                            _contextDocument.createTextNode(
                                new String(oaepParams))));
                }
                if (!encryptionMethodInformation.isEmpty()) {
                    Iterator itr = encryptionMethodInformation.iterator();
                    result.appendChild((Element) itr.next());
                }

                return (result);
            }
        }

        // <element name='EncryptionProperties' type='xenc:EncryptionPropertiesType'/>
        // <complexType name='EncryptionPropertiesType'>
        //     <sequence>
        //         <element ref='xenc:EncryptionProperty' maxOccurs='unbounded'/>
        //     </sequence>
        //     <attribute name='Id' type='ID' use='optional'/>
        // </complexType>
        private class EncryptionPropertiesImpl implements EncryptionProperties {
            private String id = null;
            private List encryptionProperties = null;

            public EncryptionPropertiesImpl() {
                encryptionProperties = new LinkedList();
            }

            public String getId() {
                return (id);
            }

            public void setId(String id) {
                this.id = id;
            }

            public Iterator getEncryptionProperties() {
                return (encryptionProperties.iterator());
            }

            public void addEncryptionProperty(EncryptionProperty property) {
                encryptionProperties.add(property);
            }

            public void removeEncryptionProperty(EncryptionProperty property) {
                encryptionProperties.remove(property);
            }

            // <element name='EncryptionProperties' type='xenc:EncryptionPropertiesType'/>
            // <complexType name='EncryptionPropertiesType'>
            //     <sequence>
            //         <element ref='xenc:EncryptionProperty' maxOccurs='unbounded'/>
            //     </sequence>
            //     <attribute name='Id' type='ID' use='optional'/>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES);
                if (null != id) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_ID, id);
                }
                Iterator itr = getEncryptionProperties();
                while (itr.hasNext()) {
                    result.appendChild(((EncryptionPropertyImpl)
                        itr.next()).toElement());
                }

                return (result);
            }
        }

        // <element name='EncryptionProperty' type='xenc:EncryptionPropertyType'/>
        // <complexType name='EncryptionPropertyType' mixed='true'>
        //     <choice maxOccurs='unbounded'>
        //         <any namespace='##other' processContents='lax'/>
        //     </choice>
        //     <attribute name='Target' type='anyURI' use='optional'/>
        //     <attribute name='Id' type='ID' use='optional'/>
        //     <anyAttribute namespace="http://www.w3.org/XML/1998/namespace"/>
        // </complexType>
        private class EncryptionPropertyImpl implements EncryptionProperty {
            private String target = null;
            private String id = null;
            private String attributeName = null;
            private String attributeValue = null;
            private List encryptionInformation = null;

            public EncryptionPropertyImpl() {
                encryptionInformation = new LinkedList();
            }

            public String getTarget() {
                return (target);
            }

            public void setTarget(String target) {
                URI tmpTarget = null;
                try {
                    tmpTarget = new URI(target);
                } catch (URI.MalformedURIException mfue) {
                    // complain
                }
                this.target = tmpTarget.toString();
            }

            public String getId() {
                return (id);
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getAttribute(String attribute) {
                return (attributeValue);
            }

            public void setAttribute(String attribute, String value) {
                attributeName = attribute;
                attributeValue = value;
            }

            public Iterator getEncryptionInformation() {
                return (encryptionInformation.iterator());
            }

            public void addEncryptionInformation(Element info) {
                encryptionInformation.add(info);
            }

            public void removeEncryptionInformation(Element info) {
                encryptionInformation.remove(info);
            }

            // <element name='EncryptionProperty' type='xenc:EncryptionPropertyType'/>
            // <complexType name='EncryptionPropertyType' mixed='true'>
            //     <choice maxOccurs='unbounded'>
            //         <any namespace='##other' processContents='lax'/>
            //     </choice>
            //     <attribute name='Target' type='anyURI' use='optional'/>
            //     <attribute name='Id' type='ID' use='optional'/>
            //     <anyAttribute namespace="http://www.w3.org/XML/1998/namespace"/>
            // </complexType>
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS, 
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTY);
                if (null != target) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_TARGET, 
                        target.toString());
                }
                if (null != id) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_ID, 
                        id);
                }
                // TODO: figure out the anyAttribyte stuff...
                // TODO: figure out the any stuff...

                return (result);
            }
        }

        // <complexType name='TransformsType'>
        //     <sequence>
        //         <element ref='ds:Transform' maxOccurs='unbounded'/>
        //     </sequence>
        // </complexType>
        private class TransformsImpl extends
		       org.apache.xml.security.transforms.Transforms 
		       implements Transforms {

			/**
			 * Construct Transforms
			 */

			public TransformsImpl() {
				super(_contextDocument);
			}

			public TransformsImpl(Document doc) {
				super(doc);
			}

			public TransformsImpl(Element element) 
				throws XMLSignatureException,
			           InvalidTransformException,
				       XMLSecurityException,
				       TransformationException {

				super(element, "");
				
			}

			public Element toElement() {

				if (_doc == null)
					_doc = _contextDocument;

				return getElement();
			}

			public org.apache.xml.security.transforms.Transforms getDSTransforms() {
				return ((org.apache.xml.security.transforms.Transforms) this);
			}


			// Over-ride the namespace
			public String getBaseNamespace() {
				return EncryptionConstants.EncryptionSpecNS;
			}

        }
    }
}
