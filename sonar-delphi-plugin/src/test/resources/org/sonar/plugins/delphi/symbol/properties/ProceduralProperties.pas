unit ProceduralProperties;

interface

type
  THandler = procedure(Arg: Integer);

  TFoo = class(TObject)
  private
    FHandler: THandler;
  public
    property Handler: THandler read FHandler;
  end;

implementation

procedure Test(Foo: TFoo; HandlerArg: Integer);
begin
  Foo.Handler(HandlerArg);
end;

end.