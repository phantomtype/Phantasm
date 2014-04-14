declare var Notify: {
    new (title : string , options? : notify.INotifyOption): notify.INotify;
    (title:string) : notify.INotify;
    (title:string, options : notify.INotifyOption) : notify.INotify;
    needsPermission() : boolean;
    requestPermission(onPermissionGrantedCallback? : Function, onPermissionDeniedCallback? : Function);
    isSupported() : boolean;
}
declare module notify {
    
    interface INotify {
        show() : void;
        onShowNotification(e : Event) : void;
        onCloseNotification() : void;
        onClickNotification() : void;
        onErrorNotification() : void;
        destroy() : void;
        close() : void;
        handleEvent(e : Event) : void;
    }
    interface INotifyOption {
        body? : string;
        icon? : string;
        tag? : string;
        notifyShow? : Function;
        nofityClose? : Function;
        notifyClick? : Function;
        notifyError? : Function;
        permissionGranted? : Function;
        permissionDenied? : Function;
    }
    

}