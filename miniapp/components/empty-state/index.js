Component({
  options: {
    addGlobalClass: true
  },
  properties: {
    text: { type: String, value: '暂无数据' },
    icon: { type: String, value: '' },
    actionText: { type: String, value: '' },
    actionClass: { type: String, value: '' }
  },
  methods: {
    onAction() { this.triggerEvent('action'); }
  }
});
