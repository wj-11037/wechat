
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
 *         &lt;element name="GetDoctorToQueuetypesResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "getDoctorToQueuetypesResult"
})
@XmlRootElement(name = "GetDoctorToQueuetypesResponse")
public class GetDoctorToQueuetypesResponse {

    @XmlElement(name = "GetDoctorToQueuetypesResult")
    protected String getDoctorToQueuetypesResult;

    /**
     * ��ȡgetDoctorToQueuetypesResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetDoctorToQueuetypesResult() {
        return getDoctorToQueuetypesResult;
    }

    /**
     * ����getDoctorToQueuetypesResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetDoctorToQueuetypesResult(String value) {
        this.getDoctorToQueuetypesResult = value;
    }

}