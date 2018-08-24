unit FunctionVirtualTest;

interface

type
  TWithVirtualFunction = class
  public
    procedure AfterConstruction; override;
    destructor Destroy; override;
    procedure a;virtual;
    function b: Integer; virtual;
  end;

implementation

{ TWithVirtualFunction }

procedure TWithVirtualFunction.a;
begin

end;

procedure TWithVirtualFunction.AfterConstruction;
begin
  inherited;

end;

function TWithVirtualFunction.b: Integer;
begin

end;

destructor TWithVirtualFunction.Destroy;
begin

  inherited;
end;

end.
