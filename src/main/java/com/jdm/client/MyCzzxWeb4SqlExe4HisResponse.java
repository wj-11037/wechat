
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
 *         &lt;element name="MyCzzxWeb4SqlExe4HisResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "myCzzxWeb4SqlExe4HisResult"
})
@XmlRootElement(name = "MyCzzxWeb4SqlExe4HisResponse")
public class MyCzzxWeb4SqlExe4HisResponse {

    @XmlElement(name = "MyCzzxWeb4SqlExe4HisResult")
    protected String myCzzxWeb4SqlExe4HisResult;

    /**
     * ��ȡmyCzzxWeb4SqlExe4HisResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMyCzzxWeb4SqlExe4HisResult() {
        return myCzzxWeb4SqlExe4HisResult;
    }

    /**
     * ����myCzzxWeb4SqlExe4HisResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMyCzzxWeb4SqlExe4HisResult(String value) {
        this.myCzzxWeb4SqlExe4HisResult = value;
    }

}
