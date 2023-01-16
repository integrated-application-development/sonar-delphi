unit InlineMethodExpansion;

interface

implementation

uses
    System.UITypes
  , Vcl.Dialogs
  ;
  
procedure Test;
begin
  MessageDlg('Spooky error!', mtError, [mbOK], 0);
end;

end.