/*
 * File: app/view/ContactUpdate.js
 *
 * This file was generated by Sencha Architect version 2.2.2.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.2.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.2.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('Simplereg.view.ContactUpdate', {
    extend: 'Ext.window.Window',

    requires: [
        'Simplereg.view.override.ContactUpdate'
    ],

    id: 'contact-update',
    itemId: 'dialog',
    width: 400,
    closeAction: 'hide',
    iconCls: 'icon-edit',
    title: 'Modify Contact',
    modal: true,

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'form',
                    bodyPadding: 10,
                    header: false,
                    title: 'Data',
                    trackResetOnLoad: true,
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'reset',
                                    iconCls: 'icon-reset',
                                    text: 'Reset'
                                },
                                {
                                    xtype: 'tbfill'
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'cancel',
                                    iconCls: 'icon-cancel',
                                    text: 'Cancel'
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'submit',
                                    iconCls: 'icon-edit',
                                    text: 'Modify Contact'
                                }
                            ]
                        }
                    ],
                    items: [
                        {
                            xtype: 'numberfield',
                            anchor: '100%',
                            hidden: true,
                            fieldLabel: 'Person Id',
                            name: 'id',
                            readOnly: true
                        },
                        {
                            xtype: 'numberfield',
                            anchor: '100%',
                            hidden: true,
                            fieldLabel: 'Person Id',
                            name: 'version',
                            readOnly: true
                        },
                        {
                            xtype: 'numberfield',
                            anchor: '100%',
                            hidden: true,
                            fieldLabel: 'Person Id',
                            name: 'personId',
                            readOnly: true
                        },
                        {
                            xtype: 'combobox',
                            anchor: '100%',
                            fieldLabel: 'Type',
                            name: 'ctype',
                            allowBlank: false,
                            editable: false,
                            displayField: 'name',
                            forceSelection: true,
                            queryMode: 'local',
                            queryParam: 'name',
                            store: 'ContactTypes',
                            valueField: 'value'
                        },
                        {
                            xtype: 'combobox',
                            anchor: '100%',
                            itemId: 'country',
                            fieldLabel: 'Country',
                            name: 'countryCode',
                            allowBlank: false,
                            displayField: 'title',
                            forceSelection: true,
                            queryMode: 'local',
                            queryParam: 'name',
                            store: 'Countries',
                            valueField: 'code'
                        },
                        {
                            xtype: 'textfield',
                            anchor: '100%',
                            fieldLabel: 'Address',
                            name: 'address',
                            allowBlank: false
                        },
                        {
                            xtype: 'textfield',
                            anchor: '100%',
                            fieldLabel: 'Phone',
                            name: 'phoneNumber'
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    }

});