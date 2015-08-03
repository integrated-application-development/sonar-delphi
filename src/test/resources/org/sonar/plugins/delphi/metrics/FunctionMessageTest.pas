unit FunctionMessageTest;

interface

uses Windows, Messages;

type
  TWithMessageFunction = class
    procedure CNCommand(var AMessage: TWMMouse); message WM_MOUSEWHEEL;
  end;

implementation

{ TWithMessageFunction }

procedure TWithMessageFunction.CNCommand(var AMessage: TWMMouse);
begin

end;

end.
