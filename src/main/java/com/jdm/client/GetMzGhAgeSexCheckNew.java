
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
 *         &lt;element name="vpkSiteId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpSex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpMyBirthday" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpMyDate4Yw" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "vpkSiteId",
    "vpSex",
    "vpMyBirthday",
    "vpMyDate4Yw"
})
@XmlRootElement(name = "GetMzGhAgeSexCheckNew")
public class GetMzGhAgeSexCheckNew {

    protected String vpkSiteId;
    protected String vpSex;
    protected String vpMyBirthday;
    protected String vpMyDate4Yw;

    /**
     * ��ȡvpkSiteId���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpkSiteId() {
        return vpkSiteId;
    }

    /**
     * ����vpkSiteId���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpkSiteId(String value) {
        this.vpkSiteId = value;
    }

    /**
     * ��ȡvpSex���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpSex() {
        return vpSex;
    }

    /**
     * ����vpSex���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpSex(String value) {
        this.vpSex = value;
    }

    /**
     * ��ȡvpMyBirthday���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpMyBirthday() {
        return vpMyBirthday;
    }

    /**
     * ����vpMyBirthday���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpMyBirthday(String value) {
        this.vpMyBirthday = value;
    }

    /**
     * ��ȡvpMyDate4Yw���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpMyDate4Yw() {
        return vpMyDate4Yw;
    }

    /**
     * ����vpMyDate4Yw���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpMyDate4Yw(String value) {
        this.vpMyDate4Yw = value;
    }

}
