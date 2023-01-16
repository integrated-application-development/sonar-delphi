unit HandlerProperty;

interface

type
  THandler = procedure(Arg1: Integer; Arg2: String; Arg3: Boolean);

  TFoo = class(TObject)
  private type
    Handler: THandler;
  public
    procedure Bar(Arg: String);

    property MyHandler: THandler read Handler;
  end;

implementation

procedure TFoo.Bar(Arg: String);
begin
  Handler(123, String(Arg), True);
end;

end.