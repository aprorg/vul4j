/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights 
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

/*
 * XSEC
 *
 * WinCAPICryptoX509:= Windows CAPI based class for handling X509 (V3) certificates
 *
 * Author(s): Berin Lautenbach
 *
 * $Id$
 *
 */

#ifndef WINCAPICRYPTOX509_INCLUDE
#define WINCAPICRYPTOX509_INCLUDE

#include <xsec/framework/XSECDefs.hpp>
#include <xsec/enc/XSECCryptoX509.hpp>

#define _WIN32_WINNT 0x0400
#include <wincrypt.h>

class WinCAPICryptoProvider;

/**
 * \brief WinCAPI implementation class for interface for X509 certificates.
 * @ingroup wincapicrypto
 *
 * The library uses classes derived from this to process X509 Certificates.
 *
 */

class DSIG_EXPORT WinCAPICryptoX509 : public XSECCryptoX509 {

public :

	/** @name Constructors and Destructors */
	//@{

	/**
	 * \brief Constructor for X509 objects
	 *
	 * The windows constructor requires RSA or DSS crypto providers, 
	 * depending on the key type within the cert.
	 *
	 * @param provRSA A handle to the PROV_RSA_FULL type provider that the
	 * interface should use when importing keys and manipulating certs
	 * @param provDSS A handle to the PROV_DSS type provider that the
	 * interface should use when importing keys and manipulating certs
	 */

	WinCAPICryptoX509(HCRYPTPROV provRSA, HCRYPTPROV provDSS);
	virtual ~WinCAPICryptoX509();

	//@}
	/** @name Key Interface methods */
	//@{

	/**
	 * \brief Return the type of the key stored in the certificate.
	 *
	 * Will extract the key from the certificate to return the appropriate
	 * type
	 *
	 */

	virtual XSECCryptoKey::KeyType getPublicKeyType();

	/**
	 * \brief Get a copy of the public key.
	 *
	 * Extracts the public key from the certificate and returns the appropriate
	 * OpenSSLCryrptoKey (DSA or RSA) object
	 *
	 */

	virtual XSECCryptoKey * clonePublicKey();

	/**
	 * \brief Returns a string that identifies the crypto owner of this library.
	 */

	virtual const XMLCh * getProviderName();

	//@}

	/** @name Load and Get the certificate */
	//@{

	/**
	 * \brief Load a certificate into the object.
	 *
	 * Take a base64 DER encoded certificate and load.
	 *
	 * @param buf A buffer containing the Base64 encoded certificate
	 * @param len The number of bytes of data in the certificate.
	 */

	virtual void loadX509Base64Bin(const char * buf, unsigned int len);

	/**
	 * \brief Get a Base64 DER encoded copy of the certificate
	 *
	 * @returns A safeBuffer containing the DER encoded certificate
	 */

	virtual safeBuffer &getDEREncodingSB(void) {return m_DERX509;}

	//@}

private:

	safeBuffer				m_DERX509;
	PCCERT_CONTEXT			mp_certContext;

	HCRYPTPROV				m_pRSA;
	HCRYPTPROV				m_pDSS;

};


#endif /* WINCAPICRYPTOX509_INCLUDE */

