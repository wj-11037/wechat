
package com.jdm.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="MyCzzxWeb4SqlSel4HisResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "myCzzxWeb4SqlSel4HisResult"
})
@XmlRootElement(name = "MyCzzxWeb4SqlSel4HisResponse")
public class MyCzzxWeb4SqlSel4HisResponse {

    @XmlElement(name = "MyCzzxWeb4SqlSel4HisResult")
    protected String myCzzxWeb4SqlSel4HisResult;

    /**
     * ��ȡmyCzzxWeb4SqlSel4HisResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMyCzzxWeb4SqlSel4HisResult() {
        return myCzzxWeb4SqlSel4HisResult;
    }

    /**
     * ����myCzzxWeb4SqlSel4HisResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMyCzzxWeb4SqlSel4HisResult(String value) {
        this.myCzzxWeb4SqlSel4HisResult = value;
    }

}
