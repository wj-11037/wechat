
package com.jdm.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vpUserId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpPwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpbasOperId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpMyInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpsign" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpIsPost" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vpUserId",
    "vpPwd",
    "vpbasOperId",
    "vpMyInfo",
    "vpsign",
    "vpIsPost"
})
@XmlRootElement(name = "XxzyyyDuanXinHuLianWang")
public class XxzyyyDuanXinHuLianWang {

    protected String vpUserId;
    protected String vpPwd;
    protected String vpbasOperId;
    protected String vpMyInfo;
    protected String vpsign;
    protected String vpIsPost;

    /**
     * ��ȡvpUserId���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpUserId() {
        return vpUserId;
    }

    /**
     * ����vpUserId���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpUserId(String value) {
        this.vpUserId = value;
    }

    /**
     * ��ȡvpPwd���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpPwd() {
        return vpPwd;
    }

    /**
     * ����vpPwd���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpPwd(String value) {
        this.vpPwd = value;
    }

    /**
     * ��ȡvpbasOperId���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpbasOperId() {
        return vpbasOperId;
    }

    /**
     * ����vpbasOperId���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpbasOperId(String value) {
        this.vpbasOperId = value;
    }

    /**
     * ��ȡvpMyInfo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpMyInfo() {
        return vpMyInfo;
    }

    /**
     * ����vpMyInfo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpMyInfo(String value) {
        this.vpMyInfo = value;
    }

    /**
     * ��ȡvpsign���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpsign() {
        return vpsign;
    }

    /**
     * ����vpsign���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpsign(String value) {
        this.vpsign = value;
    }

    /**
     * ��ȡvpIsPost���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpIsPost() {
        return vpIsPost;
    }

    /**
     * ����vpIsPost���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpIsPost(String value) {
        this.vpIsPost = value;
    }

}
