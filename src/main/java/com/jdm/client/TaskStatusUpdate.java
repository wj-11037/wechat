
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
 *         &lt;element name="vpPrescriptionIDList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpWindowNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vpStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "vpPrescriptionIDList",
    "vpWindowNo",
    "vpStatus"
})
@XmlRootElement(name = "TaskStatusUpdate")
public class TaskStatusUpdate {

    protected String vpPrescriptionIDList;
    protected String vpWindowNo;
    protected String vpStatus;

    /**
     * ��ȡvpPrescriptionIDList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpPrescriptionIDList() {
        return vpPrescriptionIDList;
    }

    /**
     * ����vpPrescriptionIDList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpPrescriptionIDList(String value) {
        this.vpPrescriptionIDList = value;
    }

    /**
     * ��ȡvpWindowNo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpWindowNo() {
        return vpWindowNo;
    }

    /**
     * ����vpWindowNo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpWindowNo(String value) {
        this.vpWindowNo = value;
    }

    /**
     * ��ȡvpStatus���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVpStatus() {
        return vpStatus;
    }

    /**
     * ����vpStatus���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVpStatus(String value) {
        this.vpStatus = value;
    }

}
